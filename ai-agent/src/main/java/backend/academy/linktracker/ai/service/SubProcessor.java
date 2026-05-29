package backend.academy.linktracker.ai.service;

public interface SubProcessor<T> {

    void process(T context);
}
