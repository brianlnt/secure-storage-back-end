package project.brianle.securestorage.event.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import project.brianle.securestorage.event.UserEvent;
import project.brianle.securestorage.service.EmailService;

@Component
@RequiredArgsConstructor
public class UserEventListener {
    private final EmailService emailService;

    @EventListener
    public void onUserEvent(UserEvent event){
        switch (event.getEventType()){
            case REGISTERATION -> emailService.sendNewAccountEmail(event.getUserEntity().getFirstName(), event.getUserEntity().getEmail(), (String)event.getData().get("key"));
            case RESETPASSWORD -> emailService.sendPasswordResetEmail(event.getUserEntity().getFirstName(), event.getUserEntity().getEmail(), (String)event.getData().get("key"));
            default -> {}
        }
    }
}
