package api;

import org.displee.CacheLibrary;

public interface CacheLoader {

    void loadItemDefinitions(CacheLibrary library);

    void loadNpcDefintions(CacheLibrary library);

    void loadSequences(CacheLibrary library);

    void loadFrameArchive(int archiveId);
}
