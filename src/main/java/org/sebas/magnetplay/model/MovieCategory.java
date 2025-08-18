package org.sebas.magnetplay.model;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MovieCategory{
    ACTION(1),
    HORROR(2),
    COMEDY(3),
    ROMANCE(4),
    FICTION(5);

    @Getter
    private final int id;

}