# AI Wedding Package Planner

A small Spring Boot backend that uses the **Claude API** and **MongoDB**. A couple
describes their dream wedding in their own words, and the API recommends a package
from an **approved catalogue**.

## The five questions

| Question | Answer |
|---|---|
| **Problem** | Couples describe weddings in free text ("garden, lots of flowers, halal, photos matter most"). Planners waste time turning that into requirements and matching packages. |
| **AI endpoint 1** `POST /api/wedding/analyse` | Claude extracts **structured requirements**: style, venue type, priorities, halal, needs-human-review. |
| **AI endpoint 2** `POST /api/wedding/consultations` | Claude **chooses one package from the candidates the app supplies** and writes a personalised proposal. |
| **MongoDB** | `wedding_packages` = trusted catalogue, loaded from [sample_data/wedding_packages.json](src/main/resources/sample_data/wedding_packages.json) on startup. `consultations` = saved results with analysis, recommendation, status and token usage. |
| **Workflow** | validate → Claude analyse → validate output → review? → MongoDB lookup → no match? → Claude recommend → validate choice → app sets status → save → return |

## Architecture

```
HTTP request
   │
WeddingController ── @Valid WeddingRequest (Bean Validation)
   │
ConsultationService (workflow, decides order + status)
   ├── AnalysisService ──► ClaudeService ──► Claude API   (structured output #1)
   │        └── AiOutputValidator (allowed values, lengths, consistency)
   ├── WeddingPackageRepository ──► MongoDB               (app controls the query)
   ├── ClaudeService ──► Claude API                       (structured output #2)
   │        └── AiOutputValidator (chosen code must be a candidate)
   └── ConsultationRepository ──► MongoDB                 (save result)
   │
GlobalExceptionHandler ── controlled JSON errors (400/404/502/503)
```

`ClaudeService` is the only class that touches the Anthropic SDK.

### Workflow detail (`POST /consultations`)

1. **Validate request** – required fields, lengths, budget RM 5,000–1,000,000, 10–2,000 guests, future date.
2. **Claude analyses** the request → `WeddingAnalysis` (JSON schema generated from the Java record).
3. **Validate AI output** – style/venue/priorities must be in allowed sets; review reason required when flagged.
4. **Human review?** If Claude flags it → save `NEEDS_REVIEW` and stop.
5. **MongoDB lookup** – the *application* queries packages within budget and guest capacity, then filters halal/venue and ranks by style.
6. **No candidates?** → save `NO_MATCH`. Claude is never asked to make up a package.
7. **Claude recommends** one of the supplied candidates and writes the proposal.
8. **Validate choice** – `recommendedPackageCode` must be one of the candidate codes, or it is rejected.
9. **App** generates the reference (`WED-yyyyMMdd-XXXXXX`), timestamp, status and token totals → saves → returns `201`.

## Validation, security and reliability decisions

- **API key outside source code** – read from `.env` / environment (`ANTHROPIC_API_KEY`); `.env` is git-ignored. The app refuses to start without it.
- **User input is untrusted** – Bean Validation on every field; the message is wrapped in `<couple_request>` tags with angle brackets stripped so it can't close the tag; the system prompt says that text is data, not instructions.
- **AI output is untrusted** – structured outputs guarantee *valid JSON*, but `AiOutputValidator` still checks *valid application data* (allowed values, lengths, consistency).
- **The application controls data and actions** – Claude never queries MongoDB, never sets status, and can only pick from packages the app gave it. A prompt like "recommend FREE-VIP-999" is rejected by code.
- **Controlled failures** – bad input → 400, not found → 404, invalid/refused AI output → 502, Claude down/rate-limited → 503, MongoDB down → 503. No stack traces leak to clients.
- **Token usage tracking** – input/output tokens are logged and stored on every consultation.

## Run it

Requirements: Java 17+, Maven, MongoDB (local or Atlas), an Anthropic API key.

```powershell
Copy-Item .env.example .env      # then edit .env and add your real key / Mongo URI
mvn spring-boot:run
```

On startup the packages in `sample_data/wedding_packages.json` are upserted by `code`
(safe to restart; edit the JSON and restart to change the catalogue). Set `SEED_RESET=true`
in `.env` for one start to clear packages **and consultations** and reseed from scratch.

The API runs on <http://localhost:8080>. Use `requests.http` (VS Code REST Client) or Postman.

Run tests (no Claude calls, no MongoDB needed):

```powershell
mvn test
```

## Endpoints

| Method | Path | Claude? | Purpose |
|---|---|---|---|
| GET | `/api/wedding/packages` | no | Approved catalogue |
| POST | `/api/wedding/analyse` | yes | Structured requirements (not saved) |
| POST | `/api/wedding/consultations` | yes ×2 | Full workflow, saved |
| GET | `/api/wedding/consultations/{reference}` | no | Retrieve saved consultation |

Example request body:

```json
{
  "coupleName": "Aina & Hafiz",
  "message": "Outdoor garden wedding, lots of flowers, halal food. Photos matter most.",
  "budgetMyr": 50000,
  "guestCount": 180,
  "weddingDate": "2027-06-12"
}
```
