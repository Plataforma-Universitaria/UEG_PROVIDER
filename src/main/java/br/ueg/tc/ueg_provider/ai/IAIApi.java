package br.ueg.tc.ueg_provider.ai;

import br.ueg.tc.pipa_integrator.enums.WeekDay;
import br.ueg.tc.pipa_integrator.institutions.info.IDiscipline;

import java.util.List;

public interface IAIApi {

    String guessMethodByRequestMessage = "Assuma que você é um processador que identifica qual serviço e método está sendo chamado em um dado texto. " +
            "Você connhece a seguinte lista de Serviços e seus métodos: ";

    String guessMethodByRequest(String request);

    String guessDisciplineNameFromIDiscipline(String disciplineIntent, List<? extends IDiscipline> disciplines);

    String startDisciplineNameQuestion = "Assuma que você é um processador que identifica qual disciplina um" +
            " texto está se referindo. As disciplinas que você conhece são: ";

    String endDisciplineNameQuestion = "Responda retornando somente o nome da disciplina por exemplo, retorne" +
            " Programação para dispositivos móveis para programação de celular." +
            " Converta todo número para algarismo romano, como por exemplo: " +
            " retorne Econometria I para Econometria 1 ou Econometria um." +
            " Entenda também quando for fornecido apenas uma sigla, como por exemplo: " +
            "retorne Prática interdisciplinar de aplicações em sistemas de informação I" +
            "para piasi 1 ou PIASI 1. Caso não encontre,retorne somente a mensagem 'NENHUMA' " +
            ".Qual a disciplina para ? ";


    String startWeekNameQuestion = "Assuma que você é um processador que identifica qual o shortname de três letras dado. Cocê conhecê os seguintes dados:"
            + "MONDAY(\"SEG\", \"Segunda-feira\"),\n" +
            "    TUESDAY(\"TER\", \"Terça-feira\"),\n" +
            "    WEDNESDAY(\"QUA\", \"Quarta-feira\"),\n" +
            "    THURSDAY(\"QUI\", \"Quinta-feira\"),\n" +
            "    FRIDAY(\"SEX\", \"Sexta-feira\"),\n" +
            "    SATURDAY(\"SAB\", \"Sábado\"),\n" +
            "    SUNDAY(\"DOM\", \"Domingo\");";

    String endWeekNameQuestion = "Responda retornando somente o shortname do dia por exemplo, retorne" +
            " SEG para segunda" +
            " QUI para 29/05/25 " +
            " Caso não encontre,retorne somente a mensagem 'NENHUMA' " +
            ".Qual o shortname para ";

    String startServiceActivationNameQuestion = "Assuma que você é um processador de serviços. Os serviços que você conhece são: ";

    String serviceActivationProgressNameQuestion = " {servico: '{0}', nomes: [{1}]}, ";

    String endServiceActivationNameQuestion = ".A partir disso responda retornando somente o nome do servico," +
            " para qual o nome: '?' seja mais proximo dos nomes dos servicos conhecidos " +
            "Caso não encontre similaridade em nenhum dos nomes dos servicos responda somente 'NENHUM'";
}
