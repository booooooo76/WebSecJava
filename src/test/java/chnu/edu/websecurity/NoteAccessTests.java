package chnu.edu.websecurity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class NoteAccessTests {

    private static final String NOTE_JSON = "{\"title\":\"Test note\",\"content\":\"From JUnit\"}";

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("1. USER can read all notes - 200")
    void userCanReadAll() throws Exception {
        mockMvc.perform(get("/api/notes").with(httpBasic("user", "user123")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("2. USER can read one note - 200")
    void userCanReadOne() throws Exception {
        mockMvc.perform(get("/api/notes/1").with(httpBasic("user", "user123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("3. USER cannot create a note - 403")
    void userCannotCreate() throws Exception {
        mockMvc.perform(post("/api/notes").with(httpBasic("user", "user123"))
                        .contentType(MediaType.APPLICATION_JSON).content(NOTE_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("4. USER cannot delete a note - 403")
    void userCannotDelete() throws Exception {
        mockMvc.perform(delete("/api/notes/1").with(httpBasic("user", "user123")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("5. MANAGER can create a note - 201")
    void managerCanCreate() throws Exception {
        mockMvc.perform(post("/api/notes").with(httpBasic("manager", "manager123"))
                        .contentType(MediaType.APPLICATION_JSON).content(NOTE_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.createdBy").value("manager"));
    }

    @Test
    @DisplayName("6. MANAGER can update a note - 200")
    void managerCanUpdate() throws Exception {
        mockMvc.perform(put("/api/notes/2").with(httpBasic("manager", "manager123"))
                        .contentType(MediaType.APPLICATION_JSON).content(NOTE_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updatedBy").value("manager"));
    }

    @Test
    @DisplayName("7. MANAGER cannot delete a note - 403")
    void managerCannotDelete() throws Exception {
        mockMvc.perform(delete("/api/notes/1").with(httpBasic("manager", "manager123")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("8. ADMIN can delete a note - 204")
    void adminCanDelete() throws Exception {
        mockMvc.perform(delete("/api/notes/3").with(httpBasic("admin", "admin123")))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("9. Anonymous request is rejected - 401")
    void anonymousIsRejected() throws Exception {
        mockMvc.perform(get("/api/notes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("10. Wrong password is rejected - 401")
    void wrongPasswordIsRejected() throws Exception {
        mockMvc.perform(get("/api/notes").with(httpBasic("manager", "wrongpass")))
                .andExpect(status().isUnauthorized());
    }
}
