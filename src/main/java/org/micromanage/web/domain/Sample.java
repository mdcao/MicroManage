package org.micromanage.web.domain;


import javax.persistence.*;

@Entity
public class Sample {

	@Id
	@Column(name="SAMPLE_ID")
	private String id;
	
	@Column(name="DESC")
	private String desc;

	String species;
	String genus;
	String strain;

	public Sample() {
	}

	public Sample(String desc) {
		this.desc = desc;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getSpecies() {
		return species;
	}

	public void setSpecies(String species) {
		this.species = species;
	}

	public String getGenus() {
		return genus;
	}

	public void setGenus(String genus) {
		this.genus = genus;
	}

	public String getStrain() {
		return strain;
	}

	public void setStrain(String strain) {
		this.strain = strain;
	}

	@Override
	public String toString() {
		return "Sample{" +
				"id=" + id +
				", desc='" + desc + '\'' +
				", species='" + species + '\'' +
				", genus='" + genus + '\'' +
				", strain='" + strain + '\'' +
				'}';
	}
}
