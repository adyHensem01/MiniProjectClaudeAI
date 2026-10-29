package wedding.com.example.weddingPackage.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import wedding.com.example.weddingPackage.dto.AiResult;
import wedding.com.example.weddingPackage.dto.AnalysisResponse;
import wedding.com.example.weddingPackage.dto.WeddingAnalysis;
import wedding.com.example.weddingPackage.dto.WeddingRequest;
import wedding.com.example.weddingPackage.model.Consultation;
import wedding.com.example.weddingPackage.model.WeddingPackage;
import wedding.com.example.weddingPackage.repository.WeddingPackageRepository;
import wedding.com.example.weddingPackage.service.AnalysisService;
import wedding.com.example.weddingPackage.service.ConsultationService;

/**
 * HTTP layer only: accept requests, validate with @Valid, delegate to services, return responses.
 */
@RestController
@RequestMapping("/api/wedding")
public class WeddingController {

    private final AnalysisService analysisService;
    private final ConsultationService consultationService;
    private final WeddingPackageRepository packageRepository;

    public WeddingController(AnalysisService analysisService, ConsultationService consultationService,
            WeddingPackageRepository packageRepository) {
        this.analysisService = analysisService;
        this.consultationService = consultationService;
        this.packageRepository = packageRepository;
    }

    /** AI endpoint 1: analyse the couple's request into structured requirements (not saved). */
    @PostMapping("/analyse")
    public AnalysisResponse analyse(@Valid @RequestBody WeddingRequest request) {
        AiResult<WeddingAnalysis> result = analysisService.analyse(request);
        return new AnalysisResponse(result.data(), result.model(), result.inputTokens(), result.outputTokens());
    }

    /** AI endpoint 2: full workflow - analyse, look up packages, recommend, save. */
    @PostMapping("/consultations")
    @ResponseStatus(HttpStatus.CREATED)
    public Consultation createConsultation(@Valid @RequestBody WeddingRequest request) {
        return consultationService.createConsultation(request);
    }

    /** Retrieve a saved consultation from MongoDB by its reference, e.g. WED-20261029-1A2B3C. */
    @GetMapping("/consultations/{reference}")
    public Consultation getConsultation(@PathVariable String reference) {
        return consultationService.getByReference(reference);
    }

    /** The approved package catalogue (trusted reference data). */
    @GetMapping("/packages")
    public List<WeddingPackage> listPackages() {
        return packageRepository.findAll();
    }
}
