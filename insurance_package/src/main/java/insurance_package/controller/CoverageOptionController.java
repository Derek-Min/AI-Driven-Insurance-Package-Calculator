package insurance_package.controller;

import insurance_package.model.CoverageOption;
import insurance_package.mongo.repository.CoverageOptionRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Profile("mongo")   // ✅ ONLY load when Mongo is enabled
@RestController
@RequestMapping("/api/coverage-options")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CoverageOptionController {

    private final CoverageOptionRepository coverageRepo;

    @GetMapping("/product/{productId}")
    public List<CoverageOption> byProduct(@PathVariable String productId) {
        return coverageRepo.findByProductId(new ObjectId(productId));
    }

    @PostMapping
    public CoverageOption add(@RequestBody CoverageOption option) {
        return coverageRepo.save(option);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CoverageOption> update(
            @PathVariable String id,
            @RequestBody CoverageOption option
    ) {
        return coverageRepo.findById(new ObjectId(id))
                .map(existing -> {
                    option.setId(existing.getId());
                    return ResponseEntity.ok(coverageRepo.save(option));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (coverageRepo.existsById(new ObjectId(id))) {
            coverageRepo.deleteById(new ObjectId(id));
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
