package br.ueg.tc.ueg_provider.infos;

import br.ueg.tc.pipa_integrator.interfaces.providers.info.ILoginData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginData implements ILoginData {
    String usernameField;
    String passwordField;
    String salutationPhrase;
    String persona;
}
