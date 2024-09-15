package com.example.demoe.Repository;

import com.example.demoe.Entity.TOKEN.Token;
import org.springframework.data.jpa.repository.EntityGraph;
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
    @EntityGraph(attributePaths = {"account"})
    @Query("select t from Token t where t.token = :token")
    Optional<Token> findByTokenWithAccount(String token);

    @Query(value = """
      select t from Token t 
      where t.token=:token 
      """)
    Optional<Token> findByToken(String token);

    @Query(value = """
      select t from Token t inner join Account u\s
      on t.account.id = u.id\s
      where u.id = :id and t.device!=:device and (t.exprired = false or t.revolked = false)\s
      """)
    List<Token> findAllByLogOutOther(Long id,String device);

    @Query(value = """
      select t from Token t 
      where t.refreshToken=:token 
      """)
    Optional<Token> findByRefreshToken(String token);
}
