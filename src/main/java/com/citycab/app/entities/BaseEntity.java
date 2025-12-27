package com.citycab.app.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @Column(length = 100, updatable = false, nullable = false)
    protected String id;

    // Chaque classe enfant doit fournir son préfixe
    protected abstract String getPrefix();

    // Options configurables par entité
    protected boolean includeDateInId() {
        return false; // inclure la date par défaut
    }

    protected String getIdSeparator() {
        return "-"; // séparateur par défaut
    }

    protected int getRandomPartLength() {
        return 10; // longueur par défaut de la partie aléatoire
    }

    protected String getDateFormat() {
        return "yyyyMMddHHmmss"; // format par défaut
    }

    @PrePersist
    protected void generateId() {
        if (this.id == null) {
            String separator = getIdSeparator();
            String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, getRandomPartLength());

            StringBuilder sb = new StringBuilder();
            sb.append(getPrefix()).append(separator).append(randomPart);

            if (includeDateInId()) {
                String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern(getDateFormat()));
                sb.append(separator).append(datePart);
            }

            this.id = sb.toString();
        }
    }

    public String getId() {
        return id;
    }
}
