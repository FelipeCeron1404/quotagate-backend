package co.edu.ucc.aiquota.generation;

public class GenerationInterruptedException extends RuntimeException {

    public GenerationInterruptedException() {
        super("La generación de texto fue interrumpida");
    }
}
