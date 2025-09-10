package com.chatapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Entity
@Table(name = "rooms")
@Setter
public class Room {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    private Set<String> participants = new HashSet<>();

    public Room() {
        this.id = UUID.randomUUID().toString();
    }

    public Room(String name, String description, String createdBy) {
        this();
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
        this.participants.add(createdBy); // Add creator as participant
    }


}