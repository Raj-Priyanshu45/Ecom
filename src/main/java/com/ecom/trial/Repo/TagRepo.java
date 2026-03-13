package com.ecom.trial.Repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecom.trial.Models.Tags;

@Repository
public interface TagRepo extends JpaRepository<Tags, Integer> {
    Optional<Tags> findBySlug(String slug);

    List<Tags> findBySlugIn(List<String> slugs);
}