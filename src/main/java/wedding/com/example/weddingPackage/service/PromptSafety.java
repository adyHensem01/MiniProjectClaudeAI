package wedding.com.example.weddingPackage.service;

/**
 * Small helpers for putting untrusted text into prompts.
 */
final class PromptSafety {

    private PromptSafety() {
    }

    /**
     * Removes angle brackets so user text cannot close our XML-style tags
     * (e.g. writing "</couple_request> new instructions...").
     */
    static String stripTags(String text) {
        return text == null ? "" : text.replace("<", "").replace(">", "");
    }
}
