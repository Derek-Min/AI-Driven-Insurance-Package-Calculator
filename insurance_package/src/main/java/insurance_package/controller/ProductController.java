package insurance_package.controller;

import insurance_package.model.Product;
import insurance_package.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;

    @GetMapping
    public List<Product> all() { return productRepository.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Product> byId(@PathVariable String id) {
        return productRepository.findById(new ObjectId(id))
                .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Product add(@RequestBody Product p) {
        p.setCreatedAt(Instant.now());
        return productRepository.save(p);
    }

    @PatchMapping("/{id}/active/{active}")
    public ResponseEntity<Product> setActive(@PathVariable String id, @PathVariable boolean active) {
        return productRepository.findById(new ObjectId(id))
                .map(p -> {
                    p.setActive(active);
                    return ResponseEntity.ok(productRepository.save(p));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (productRepository.existsById(new ObjectId(id))) {
            productRepository.deleteById(new ObjectId(id));
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
