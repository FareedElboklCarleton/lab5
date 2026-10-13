package org.example;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "addressbooks", collectionResourceRel = "addressbooks")
public interface AddressBookRepository extends CrudRepository<AddressBook, Integer> {
}
