package hu.emraxxor.web.ide.data.type.docker.consumer;

import java.util.function.Consumer;

import lombok.Getter;
import lombok.Setter;

/**
 * @Source: testcontainers-java
 * 
 * @author attila
 *
 * @param <SELF>
 */
public abstract class BaseConsumer<SELF extends BaseConsumer<SELF>> implements Consumer<OutputFrame> {
    @Getter
    @Setter
    private boolean removeColorCodes = true;

    public SELF withRemoveAnsiCodes(boolean removeAnsiCodes) {
        this.removeColorCodes = removeAnsiCodes;
        return (SELF) this;
    }
}