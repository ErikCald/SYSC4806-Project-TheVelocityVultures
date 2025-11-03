package vv.pms;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import vv.pms.ui.records.ProfessorRecord;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

class ProfessorWebIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testCreateProfessor() {
        ProfessorRecord dto = new ProfessorRecord(null, "John Doe", "john@example.com");

        ResponseEntity<ProfessorRecord> response = restTemplate.postForEntity(
                "/api/professors",
                dto,
                ProfessorRecord.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isNotNull(); // created ID from DB
        assertThat(response.getBody().name()).isEqualTo("John Doe");
    }

    @Test
    void testDuplicateProfessorFails() {
        ProfessorRecord dto = new ProfessorRecord(null, "Test", "dup@example.com");
        restTemplate.postForEntity("/api/professors", dto, ProfessorRecord.class);

        ResponseEntity<ProfessorRecord> response = restTemplate.postForEntity(
                "/api/professors",
                dto,
                ProfessorRecord.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testGetAllProfessors() {
        ProfessorRecord dto = new ProfessorRecord(null, "Mark", "mark@example.com");
        restTemplate.postForEntity("/api/professors", dto, ProfessorRecord.class);

        ResponseEntity<ProfessorRecord[]> response =
                restTemplate.getForEntity("/api/professors", ProfessorRecord[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void testUpdateProfessor() {
        ProfessorRecord dto = new ProfessorRecord(null, "Bob", "bob@example.com");
        ProfessorRecord created = restTemplate.postForEntity("/api/professors", dto, ProfessorRecord.class).getBody();

        ProfessorRecord updatedDTO = new ProfessorRecord(created.id(), "Bob2", "bob2@example.com");

        HttpEntity<ProfessorRecord> request = new HttpEntity<>(updatedDTO);
        ResponseEntity<ProfessorRecord> response = restTemplate.exchange(
                "/api/professors/" + created.id(),
                HttpMethod.PUT,
                request,
                ProfessorRecord.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().name()).isEqualTo("Bob2");
        assertThat(response.getBody().email()).isEqualTo("bob2@example.com");
    }

    @Test
    void testDeleteProfessor() {
        ProfessorRecord dto = new ProfessorRecord(null, "DeleteMe", "delete@example.com");
        ProfessorRecord created = restTemplate.postForEntity("/api/professors", dto, ProfessorRecord.class).getBody();

        restTemplate.delete("/api/professors/" + created.id());

        ResponseEntity<ProfessorRecord[]> response =
                restTemplate.getForEntity("/api/professors", ProfessorRecord[].class);

        // confirm not found anymore
        assertThat(response.getBody()).doesNotContain(created);
    }
}