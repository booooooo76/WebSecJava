package chnu.edu.websecurity;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final Map<Long, Note> notes = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong();

    @GetMapping
    public Collection<Note> getAll() {
        return notes.values();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Note> getById(@PathVariable Long id) {
        Note note = notes.get(id);
        if (note == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(note);
    }

    @PostMapping
    public ResponseEntity<Note> create(@RequestBody Note request) {
        long id = idSequence.incrementAndGet();
        Note note = new Note(id, request.getTitle(), request.getContent());
        notes.put(id, note);
        return ResponseEntity.status(HttpStatus.CREATED).body(note);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Note> update(@PathVariable Long id, @RequestBody Note request) {
        Note existing = notes.get(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        existing.setTitle(request.getTitle());
        existing.setContent(request.getContent());
        return ResponseEntity.ok(existing);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Note removed = notes.remove(id);
        if (removed == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
