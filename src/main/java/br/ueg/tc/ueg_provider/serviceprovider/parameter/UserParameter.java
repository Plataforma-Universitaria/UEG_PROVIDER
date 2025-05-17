package br.ueg.tc.ueg_provider.serviceprovider.parameter;

import br.ueg.tc.pipa_integrator.enums.ParameterType;
import br.ueg.tc.pipa_integrator.institutions.IBaseInstitutionProvider;
import br.ueg.tc.pipa_integrator.serviceprovider.parameters.AParameter;

public class UserParameter extends AParameter {

    @Override
    public ParameterType getType() {
        return ParameterType.AUTO;
    }

    @Override
    public Object getDefaultValue() {
        return null;
    }

    @Override
    public Object getValueFromInstitution(IBaseInstitutionProvider institution) {
        return institution.getUserData();
    }

    @Override
    public Object getObjectValue(String value) {
        return null;
    }
}
