package chnu.edu.websecurity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private static final Logger audit = LoggerFactory.getLogger("AUDIT");

    private final Map<Long, Note> notes = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong();

    public NoteController() {
        seed("Shopping list", "Milk, bread, eggs");
        seed("Lecture", "CSRF and audit in Spring Security");
        seed("Reminder", "Submit Task 4 to Moodle");
    }

    private void seed(String title, String content) {
        long id = idSequence.incrementAndGet();
        Note note = new Note(id, title, content);
        note.setCreatedAt(Instant.now());
        note.setCreatedBy("system");
        notes.put(id, note);
    }

    @GetMapping
    public Collection<Note> getAll(Principal principal) {
        audit.info("user={} action=READ_ALL", principal.getName());
        return notes.values();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Note> getById(@PathVariable Long id, Principal principal) {
        Note note = notes.get(id);
        audit.info("user={} action=READ id={} found={}", principal.getName(), id, note != null);
        if (note == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(note);
    }

    @PostMapping
    public ResponseEntity<Note> create(@RequestBody Note request, Principal principal) {
        long id = idSequence.incrementAndGet();
        Note note = new Note(id, request.getTitle(), request.getContent());
        note.setCreatedAt(Instant.now());
        note.setCreatedBy(principal.getName());
        notes.put(id, note);
        audit.info("user={} action=CREATE id={}", principal.getName(), id);
        return ResponseEntity.status(HttpStatus.CREATED).body(note);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Note> update(@PathVariable Long id, @RequestBody Note request, Principal principal) {
        Note existing = notes.get(id);
        audit.info("user={} action=UPDATE id={} found={}", principal.getName(), id, existing != null);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        existing.setTitle(request.getTitle());
        existing.setContent(request.getContent());
        existing.setUpdatedAt(Instant.now());
        existing.setUpdatedBy(principal.getName());
        return ResponseEntity.ok(existing);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Principal principal) {
        Note removed = notes.remove(id);
        audit.info("user={} action=DELETE id={} found={}", principal.getName(), id, removed != null);
        if (removed == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
