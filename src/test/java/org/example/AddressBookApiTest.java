package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// Load real Spring components with a separate test database.
// Handle simulated HTTP requests without starting a web server.
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:addressbook-api-test;DB_CLOSE_DELAY=-1")
@AutoConfigureRestTestClient
class AddressBookApiTest {

    @Autowired
    private RestTestClient client;

    @Autowired
    private BuddyInfoRepository buddyInfoRepository;

    @Autowired
    private AddressBookRepository addressBookRepository;

    @BeforeEach
    void clearTestData() {
        // Remove books first because they can reference buddies, including the startup demo data.
        addressBookRepository.deleteAll();
        buddyInfoRepository.deleteAll();
    }

    @Test
    void createBuddyAndRetrieveSavedValues() {
        var response = client.post()
                .uri("/api/buddies")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("name", "Sam", "phoneNumber", "613-555-1234", "address", "123 Main Street, Ottawa"))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectBody().returnResult();

        URI location = response.getResponseHeaders().getLocation(); // Tells us the location of newly created buddy
        assertNotNull(location);
        assertEquals(1, buddyInfoRepository.count());

        // Follow the server's Location header instead of assuming a generated ID.
        client.get().uri(location)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Sam")
                .jsonPath("$.phoneNumber").isEqualTo("613-555-1234")
                .jsonPath("$.address").isEqualTo("123 Main Street, Ottawa");
    }

    @Test
    void listBuddiesReturnsSavedBuddies() {
        buddyInfoRepository.save(new BuddyInfo("Sam", "613-555-1234"));
        buddyInfoRepository.save(new BuddyInfo("Alex", "613-555-5678"));

        client.get().uri("/api/buddies")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$._embedded.buddies.length()").isEqualTo(2)
                .jsonPath("$._embedded.buddies[*].name")
                .value((List<String> names) -> assertThat(names).containsExactlyInAnyOrder("Sam", "Alex"))
                .jsonPath("$._embedded.buddies[?(@.name == 'Sam')].phoneNumber")
                .isEqualTo(List.of("613-555-1234"))
                .jsonPath("$._embedded.buddies[?(@.name == 'Alex')].phoneNumber")
                .isEqualTo(List.of("613-555-5678"));
    }

    @Test
    void createAddressBookAndRetrieveIt() {
        var response = client.post()
                .uri("/api/addressbooks")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of())
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectBody().returnResult();

        URI location = response.getResponseHeaders().getLocation();
        assertNotNull(location);
        assertEquals(1, addressBookRepository.count());

        client.get().uri(location)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$._links.self.href").isEqualTo(location.toString());
    }

    @Test
    void listAddressBooksReturnsSavedBooks() {
        AddressBook firstBook = addressBookRepository.save(new AddressBook());
        AddressBook secondBook = addressBookRepository.save(new AddressBook());

        client.get().uri("/api/addressbooks")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$._embedded.addressbooks.length()").isEqualTo(2)
                .jsonPath("$._embedded.addressbooks[*]._links.self.href")
                .value((List<String> links) -> assertThat(links).containsExactlyInAnyOrder(
                        "http://localhost/api/addressbooks/" + firstBook.getId(),
                        "http://localhost/api/addressbooks/" + secondBook.getId()));
    }

    @Test
    void listAddressBookBuddiesReturnsOnlyItsBuddies() {
        // Buddies must be saved before assigning them to a book because saves do not cascade.
        BuddyInfo sam = buddyInfoRepository.save(new BuddyInfo("Sam", "613-555-1234"));
        BuddyInfo alex = buddyInfoRepository.save(new BuddyInfo("Alex", "613-555-5678"));
        BuddyInfo otherBuddy = buddyInfoRepository.save(new BuddyInfo("Taylor", "613-555-9999"));

        AddressBook book = new AddressBook();
        book.addBuddy(sam);
        book.addBuddy(alex);
        AddressBook savedBook = addressBookRepository.save(book);

        AddressBook otherBook = new AddressBook();
        otherBook.addBuddy(otherBuddy);
        addressBookRepository.save(otherBook);

        client.get().uri("/api/addressbooks/{id}/buddies", savedBook.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$._embedded.buddies.length()").isEqualTo(2)
                .jsonPath("$._embedded.buddies[*].name")
                .value((List<String> names) -> assertThat(names).containsExactlyInAnyOrder("Sam", "Alex"))
                .jsonPath("$._embedded.buddies[?(@.name == 'Sam')].phoneNumber")
                .isEqualTo(List.of("613-555-1234"))
                .jsonPath("$._embedded.buddies[?(@.name == 'Alex')].phoneNumber")
                .isEqualTo(List.of("613-555-5678"));
    }

    @Test
    void getMissingBuddyReturnsNotFound() {
        client.get().uri("/api/buddies/{id}", Integer.MAX_VALUE)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody();
    }

    @Test
    void getMissingAddressBookReturnsNotFound() {
        client.get().uri("/api/addressbooks/{id}", Integer.MAX_VALUE)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody();
    }

    @Test
    void listBuddiesReturnsEmptyCollectionWhenNoBuddiesExist() {
        client.get().uri("/api/buddies")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$._embedded.buddies").isEqualTo(List.of());
    }

    @Test
    void listAddressBooksReturnsEmptyCollectionWhenNoBooksExist() {
        client.get().uri("/api/addressbooks")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$._embedded.addressbooks").isEqualTo(List.of());
    }

    @Test
    void createBuddyWithMalformedJsonReturnsBadRequestWithoutSaving() {
        client.post().uri("/api/buddies")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"name\":")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody();

        assertEquals(0, buddyInfoRepository.count());
    }

    @Test
    void createAddressBookWithMalformedJsonReturnsBadRequestWithoutSaving() {
        client.post().uri("/api/addressbooks")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"buddies\":")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody();

        assertEquals(0, addressBookRepository.count());
    }
}
