package org.micromanage.web.repository;

import org.micromanage.web.domain.Sample;
import org.springframework.data.repository.CrudRepository;

public interface SampleRepository extends CrudRepository<Sample, String> {

}
