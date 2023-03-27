package site.madhavjha.betterreadsdataloader.book;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;
import site.madhavjha.betterreadsdataloader.author.Author;

@Repository
public interface BookRepository extends CassandraRepository<Book, String> {
}
