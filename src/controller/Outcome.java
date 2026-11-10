package controller;

// Resposta de um comando para a view: sucesso ou falha, sempre com mensagem pronta
public record Outcome(boolean success, String message) {
    static Outcome ok(String message) { return new Outcome(true, message); }
    static Outcome fail(String message) { return new Outcome(false, message); }
}