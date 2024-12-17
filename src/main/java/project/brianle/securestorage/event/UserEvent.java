package project.brianle.securestorage.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import project.brianle.securestorage.entity.UserEntity;
import project.brianle.securestorage.enumeration.EventType;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class UserEvent {
    private UserEntity userEntity;
    private EventType eventType;
    private Map<?,?> data;
}
