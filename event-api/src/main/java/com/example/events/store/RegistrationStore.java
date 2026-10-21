package com.example.events.store;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.events.model.Registration;

public interface RegistrationStore extends JpaRepository<Registration, String> {

	List<Registration> findByAttendeeRefOrderByRequestedAtAscRegistrationIdAsc(String attendeeRef);

}
