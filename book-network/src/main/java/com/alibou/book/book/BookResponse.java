package com.alibou.book.book;

import com.alibou.book.feedback.Feedback;
import com.alibou.book.history.BookTransactionHistory;
import com.alibou.book.user.User;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookResponse {

    private Integer id;

    private String title;

    private String authorName;

    private String isbn;

    private String synopsis;

    private String owner;

    private byte [] cover;

    private double rate;

    private boolean archived;

    private boolean shareable;
}
