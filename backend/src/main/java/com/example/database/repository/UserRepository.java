package com.example.database.repository;

import com.example.database.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Reposytorium dla encji {@link User}.
 * <p>
 * Interfejs dziedziczy po {@link JpaRepository}, zapewniając dostęp do podstawowych operacji CRUD oraz do zapytań specyficznych
 * dla encji {@link User}. Repozytorium jest oznaczone adnotacją {@link Repository}, co umożliwia wstrzykiwanie zależności przez
 * Springa.
 * <p>
 * Zawiera metody umożliwiające wyszukiwanie użytkowników na podstawie nazwy użytkownika i adresu e-mail, a także sprawdzanie
 * istnienia użytkownika w bazie danych na podstawie tych danych.
 *
 * @author Jakub
 * @version 1.0.0
 * @since 1.0.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer>
{
  /**
   * Znajduje użytkownika na podstawie nazwy użytkownika.
   *
   * @param username Nazwa użytkownika, którego profil ma zostać znaleziony.
   * @return Opcjonalny użytkownik o podanej nazwie użytkownika.
   */
  Optional<User> findByUsername(String username);

  /**
   * Znajduje użytkownika na podstawie adresu e-mail.
   *
   * @param email Adres e-mail, na podstawie którego ma zostać znaleziony użytkownik.
   * @return Opcjonalny użytkownik o podanym adresie e-mail.
   */
  Optional<User> findByEmail(String email);

  /**
   * Sprawdza, czy użytkownik o podanej nazwie użytkownika już istnieje w bazie danych.
   *
   * @param username Nazwa użytkownika, którą należy sprawdzić.
   * @return True, jeśli użytkownik o podanej nazwie istnieje, false w przeciwnym przypadku.
   */
  boolean existsByUsername(String username);

  /**
   * Sprawdza, czy użytkownik o podanym adresie e-mail już istnieje w bazie danych.
   *
   * @param email Adres e-mail, który należy sprawdzić.
   * @return True, jeśli użytkownik o podanym adresie e-mail istnieje, false w przeciwnym przypadku.
   */
  boolean existsByEmail(String email);
}