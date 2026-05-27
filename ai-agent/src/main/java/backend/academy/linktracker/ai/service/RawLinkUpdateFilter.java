package backend.academy.linktracker.ai.service;

import backend.academy.linktracker.ai.model.RawLinkUpdate;

public interface RawLinkUpdateFilter {

    boolean filter(RawLinkUpdate rawLinkUpdate);

}
