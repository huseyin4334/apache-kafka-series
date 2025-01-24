package com.example.notificationservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "processed_events")
public class CreatedProduct implements Serializable {

    // Serial version UID is used to ensure that during deserialization the same class (that was used during serialization) is loaded.
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String id; // Product ID

    @Column(name = "message_id", unique = true, nullable = false)
    private String messageId; // Message ID
}
