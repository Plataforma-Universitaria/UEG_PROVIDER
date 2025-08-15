package br.ueg.tc.ueg_provider.serviceprovider;

import br.ueg.tc.pipa_integrator.annotations.ServiceProviderClass;
import br.ueg.tc.pipa_integrator.annotations.ServiceProviderMethod;
import br.ueg.tc.pipa_integrator.interfaces.platform.IUser;

import static br.ueg.tc.ueg_provider.UEGEndpoint.UEG_CONTATOS;

@ServiceProviderClass(personas = {"Convidado", "Aluno", "Professor"})
public class FreeAccessService extends InstitutionService {

    public FreeAccessService() {
        super();
    }

    public FreeAccessService(IUser user) {
        super(user);
    }

    @ServiceProviderMethod(activationPhrases = {
            "Me conte a história da UEG",
            "Criação da UEG",
            "Como foi feita a UEG"
    })
    public String getUEGHistory() {
        return """
                A criação da Universidade Estadual de Goiás (UEG) remonta à década de 1940, com registros de intenção já naquela época. 
                No entanto, foi apenas em 1987, sob o governo de Henrique Santillo, que os estudos para sua implantação ganharam força. 
                Em 1990, a Faculdade de Ciências Econômicas de Anápolis (Facea), fundada em 1961, foi transformada na Universidade Estadual de Anápolis (Uniana).
                
                A UEG foi oficialmente criada em 1999 por meio da Lei nº 13.456, ao incorporar outras 14 instituições de ensino superior de Goiás.
                Sua sede está localizada em Anápolis.
                
                O principal objetivo da universidade era formar professores, já que apenas 32% tinham ensino superior na época. 
                Atualmente, a UEG oferece cursos de graduação (licenciatura, bacharelado, tecnólogo) e pós-graduação (lato e stricto sensu) 
                em diversas áreas do conhecimento.
                """;
    }

    @ServiceProviderMethod(activationPhrases = {
            "Contato",
            "telefone da UEG",
            "Email da secretaria"
    })
    public String getInstitutionContact() {
        return """
                 Contatos institucionais da UEG:
                
                • Número geral: (62) 3328-1433
                
                Campus Central:
                • Endereço: Rodovia BR-153, Quadra Área, Km 99, Fazenda Barreiro do Meio, Anápolis/GO, CEP: 75132-400
                
                 Secretaria Acadêmica Central:
                • Responsável: Brandina Fátima Mendonça de Castro Andrade
                • Telefone: (62) 3328-1402 / (62) 3328-1152 Ramais 9716 e 9715
                • E-mail: gsec.central@ueg.br
                • SEI: 20259
                
                 Coordenação de Diplomas:
                • Responsável: Jane Aparecida Borges Arantes
                • Telefone: (62) 3328-1152
                • E-mail: diploma.prg@ueg.br
                • SEI: 16128
                
                 Coordenação de Gestão das Secretarias Acadêmicas:
                • Responsável: Lílian Lopes Fernandes
                • Telefone: (62) 3328-1135
                • E-mail: gsec.central@ueg.br
                • SEI: 20261
                
                 Secretaria do Campus Central:
                • E-mail: secretaria.campuscentral@ueg.br
                
                 Para mais informações, acesse:\s""" + UEG_CONTATOS;
    }
}
