package com.example.demoe.Repository;

import com.example.demoe.Entity.TOKEN.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token,Long> {
    @Query(value = """
      select t from Token t inner join Account u\s
      on t.account.id = u.id\s
      where u.id = :id and t.device=:device and (t.exprired = false or t.revolked = false)\s
      """)
    List<Token> findAllByUserId(Long id,String device);
    Optional<Token> findByToken(String token);
}
