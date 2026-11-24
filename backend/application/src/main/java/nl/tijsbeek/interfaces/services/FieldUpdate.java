package nl.tijsbeek.interfaces.services;

public record FieldUpdate<T>(boolean supplied, T value) {
}
