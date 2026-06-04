package com.shotdo.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.shotdo.backend.entity.Todo;
import com.shotdo.backend.entity.User;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {
    List<Todo> findByUserAndDate(User user, LocalDate date);
}
