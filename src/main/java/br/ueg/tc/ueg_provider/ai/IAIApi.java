package br.ueg.tc.ueg_provider.ai;

import br.ueg.tc.pipa_integrator.interfaces.providers.info.IDiscipline;

import java.time.LocalDate;
import java.util.List;

public interface IAIApi {

    String guessMethodByRequestMessage = """
        Você é um classificador de comandos. Com base na mensagem do usuário, identifique qual serviço e método devem ser chamados.
        Abaixo está a lista de serviços e métodos disponíveis:
        """;

    String guessMethodByRequest(String request);

    String guessDisciplineNameFromIDiscipline(String disciplineIntent, List<? extends IDiscipline> disciplines);

    String startDisciplineNameQuestion = """
        Você é uma ferramenta que identifica a disciplina pelo seu nome comum.
        As disciplinas conhecidas são:
        """;

    String endDisciplineNameQuestion = """
            Retorne o nome completo mais próximo:
            - Corrija erros: "prog web" → "Programação para Web"
            - Decodifique abreviações: "infra" -> "INFRAESTRUTURA"
            - Use algarismos romanos: "Econometria 1" → "Econometria I"
            - Decodifique siglas: "PIASI" → "Prática Interdisciplinar..." "prog" -> "programação" "mobile" -> dispositivos móveis
            Se nenhuma for compatível, retorne: 'NENHUMA'.
            Qual é a disciplina?
           \s""";


    String startWeekNameQuestion = """
    Você é uma ferramenta, um identificador de dias da semana. Suas respostas válidas são:
    {SEG, TER, QUA, QUI, SEX, SAB, DOM, NENHUMA}
    
    Considere o dia de hoje como o dia:
    """ + LocalDate.now() + " e o dia da semana como o dia:" + LocalDate.now().getDayOfWeek().name() + " O ano atual é : " + LocalDate.now().getYear();

    String endWeekNameQuestion = """
    Retorne apenas um dos shortnames válidos (SEG, TER, QUA, QUI, SEX, SAB, DOM) de acordo com o conteúdo da pergunta.
    Regras:
    - Para nomes de dias ("segunda", "terça-feira", etc.), retorne o shortname correspondente:
     Exemplo:
        - segunda → SEG
        - domingo -> DOM
    - Para datas no formato dd/MM/yy ou dd/MM/yyyy, retorne o shortname referente ao dia da semana daquela data: "29/05/25" → QUI.
    - Para palavras relativas ao tempo:
        - "hoje" → shortname do dia atual.
        - "amanhã" → shortname do dia seguinte.
        - "ontem" → shortname do dia anterior.
    - Para meses ou frases como "15 de abril", interprete como uma data válida.
    
    Caso não seja possível identificar o dia, retorne exatamente: 'NENHUMA'.

    Qual é o shortname para:
    """;


    String startServiceActivationNameQuestion = """
        Você é um processador que identifica o serviço a ser ativado.
        Os serviços conhecidos são:
        """;

    String serviceActivationProgressNameQuestion = " {servico: '{0}', nomes: [{1}]}, ";

    String endServiceActivationNameQuestion = """
        A partir do nome informado, retorne apenas o nome do serviço mais semelhante.
        Se não houver similaridade suficiente, retorne exatamente: 'NENHUM'.
        Serviço correspondente ao nome:
        """;
}
