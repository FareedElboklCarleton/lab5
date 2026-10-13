package org.example;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "buddies", collectionResourceRel = "buddies")
public interface BuddyInfoRepository extends CrudRepository<BuddyInfo, Integer> {
}
