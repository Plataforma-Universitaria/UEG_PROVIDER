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

    String disciplineNameQuestion = """
                Ignore qualquer contexto anterior.
            
                    Você é uma ferramenta de mapeamento de disciplinas acadêmicas. \s
                    Sua única tarefa é analisar um nome de disciplina fornecido de forma abreviada, incompleta ou incorreta e retornar **exclusivamente** o nome completo da disciplina, em letras MAIÚSCULAS, exatamente como listado na tabela de referência.
                    Se não houver correspondência, responda apenas com: NENHUMA. \s
                    Não escreva nada além disso.
            
                    Regras obrigatórias:
                    - Corrija abreviações. Ex.: "prog web" → "PROGRAMAÇÃO WEB".
                    - Decodifique termos incompletos. Ex.: "infra" → "INFRAESTRUTURA PARA SISTEMAS DE INFORMAÇÃO".
                    - Substitua números arábicos por algarismos romanos. Ex.: "Econometria 1" → "ECONOMETRIA I".
                    - Decodifique siglas. Ex.: "PIASI" → "PRÁTICA INTERDISCIPLINAR DE APLICAÇÕES EM SISTEMAS DE INFORMAÇÃO".
                    - Utilize somente nomes exatos da lista de disciplinas conhecidas (abaixo).
            
                    Lista de disciplinas conhecidas:
            
            """;
    String courseNameQuestion = """
                            Ignore qualquer contexto anterior.
            
                Você é um motor de busca de compatibilidade de cursos.
                    Sua única tarefa é analisar o campo **curso** da lista com  a entrada que será passada e retornar o id_curso.
            
                    Com base na correspondência mais provável entre o nome fornecido e o valor da chave "curso" dentro da lista JSON, você deve retornar **EXCLUSIVAMENTE** o valor da chave **"id_curso"** do objeto correspondente.
            
                    Se houver mais de uma correspondência altamente provável, escolha o objeto com o "perfil" mais genérico (por exemplo, aquele sem um perfil específico se possível).
            
                    Se não for possível encontrar nenhuma correspondência razoável, responda apenas com: **NENHUMA**.
            
                    Regras obrigatórias para correspondência:
                    1.  **Priorize a Busca:** Use as chaves "curso" e "modalidade" para encontrar a melhor correspondência.
                    2.  **Tratamento de Entrada:** Corrija abreviações, decodifique termos incompletos e siglas na entrada do usuário para buscar o nome completo mais compatível na lista.
                        * Exemplo: Se a entrada for "sistemas", procure a correspondência para "Sistemas de Informação".
                        * Exemplo: Se a entrada for "si", procure a correspondência para "Sistemas de Informação".
                        * Exemplo: Se a entrada for "civil", procure a correspondência para "Engenharia Civil".
            
                    ---
                    Lista de cursos conhecidos:
            
            """;

    String studentNameQuestion = """
                Ignore qualquer contexto anterior.
            
                    Você é um sistema de mapeamento de identificação de estudantes.
                        Sua única tarefa é analisar um **nome de estudante** fornecido (que pode estar incompleto, abreviado ou incorreto) e encontrar o registro mais compatível na lista de dados de estudantes que você receberá.
            
                        Com base na correspondência mais provável, você deve retornar **EXCLUSIVAMENTE** o valor da chave **"tcu_id"** do objeto correspondente.
            
                        Se for encontrado mais de um nome similar, utilize critérios de desempate como a data de nascimento ou o nome completo mais extenso, se esses dados estiverem disponíveis na lista, para escolher o melhor ID.
            
                        Se não for possível encontrar nenhuma correspondência razoável para o nome fornecido, responda apenas com: **NENHUMA**.
            
                        Regras obrigatórias para correspondência:
                        1.  **Tratamento de Entrada:** Você deve ser tolerante a erros de digitação leves, abreviações (Ex: "J. Silva" para "João Silva"), e inversão de nomes.
                        2.  **Saída Exclusiva:** A resposta deve ser *apenas* o número do `tcu_id`, sem nenhuma explicação ou texto adicional.
            
                    Lista de nomes conhecidos:
            
            """;

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
