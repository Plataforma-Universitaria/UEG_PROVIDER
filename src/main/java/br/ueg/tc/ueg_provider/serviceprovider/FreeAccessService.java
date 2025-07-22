package br.ueg.tc.ueg_provider.serviceprovider;

import br.ueg.tc.pipa_integrator.annotations.ServiceProviderClass;
import br.ueg.tc.pipa_integrator.annotations.ServiceProviderMethod;
import br.ueg.tc.pipa_integrator.interfaces.platform.IUser;

@ServiceProviderClass(personas = {"Convidado", "Aluno", "Professor"})
public class FreeAccessService extends InstitutionService {

    public FreeAccessService() {
        super();
    }
    public FreeAccessService(IUser user){
        super(user);
    }

    @ServiceProviderMethod(activationPhrases = {"Me conte a história da UEG", "Criação da UEG", "Como foi feita a UEG"})
    public String getUEGHistory(){
        return "A ideia de estabelecer uma universidade estadual em Goiás remonta à década de 1940. Nesse período, conforme documentos do Conselho Estadual de Educação, já se observava um movimento nessa direção. Contudo, o projeto não se concretizou naquele momento, sendo retomado só em 1987 pelo então governador Henrique Santillo. Ele criou uma comissão com o propósito de conduzir estudos preliminares para a criação e implantação da Universidade Estadual de Anápolis, o que efetivamente ocorreu em 1990. Naquele ano, a Faculdade de Ciências Econômicas de Anápolis (Facea), fundada em 1961, foi convertida na Universidade Estadual de Anápolis (Uniana).\n" +
                "\n" +
                "No ano de 1999, A Universidade Estadual de Goiás (UEG) teve sua origem na transformação da Uniana e na incorporação de outras 14 instituições de ensino superior, por meio da Lei 13.456. Sua sede foi estabelecida na cidade de Anápolis. A UEG nasceu com o objetivo central desenvolver um programa específico voltado para os trabalhadores da educação do Estado, oferecendo-lhes formação de nível superior. Isso se justificava pela constatação de que apenas 32% dos professores possuíam graduação na época. Mas a UEG não se limitou a formar professores e hoje oferta cursos em todas as áreas do conhecimento. Atualmente, a instituição oferece licenciaturas, bacharelados, cursos superiores de tecnologias, além de pós-graduações lato e stricto sensu.";
    }

    //TODO: [FUNCIONALIDADE PENDENTE] Implementar 'getAvailableCourses' para listar os cursos oferecidos pela UEG.
    // Utilizará um endpoint público.
    // Adicionar @ServiceProviderMethod com frases como "quais cursos a UEG tem?", "listar cursos de graduação".

}

