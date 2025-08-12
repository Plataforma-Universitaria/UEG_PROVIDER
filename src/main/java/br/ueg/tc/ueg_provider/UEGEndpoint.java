package br.ueg.tc.ueg_provider;

public interface UEGEndpoint {
    String VALIDA_LOGIN = "https://www.app.ueg.br/auth/acesso/valida_login";
    String ENTRA_PORTAL_ESTUDANTE = "https://www.app.ueg.br/portal_estudante";
    String ENTRA_PORTAL_PROFESSOR = "https://www.app.ueg.br/portal_docente";
    String PERFIL = "https://www.app.ueg.br/fenix-back-end/perfil/";
    String HORARIO_AULA = "https://www.app.ueg.br/fenix-back-end/horario_aula";
    String DADOS_DISCIPLINAS = "https://www.app.ueg.br/fenix-back-end/percurso_academico/lista_matriculas_aluno/?acu_id=";
    String DADOS_ACADEMICOS = "https://www.app.ueg.br/fenix-back-end/perfil/dados_academicos";
    String DADOS_ACADEMICOS_OBRIGATORIOS = "https://www.app.ueg.br/fenix-back-end/perfil/dados_academicos";
    String DADOS_ACADEMICOS_OPCIONAIS = "https://www.app.ueg.br/fenix-back-end/perfil/dados_academicos";
    String DADOS_ACADEMICOS_NUCLEO_LIVRE = "https://www.app.ueg.br/fenix-back-end/perfil/dados_academicos";
    String DADOS_ACADEMICOS_SEMINARIO = "https://www.app.ueg.br/fenix-back-end/perfil/dados_academicos";
    String GERAR_NOVA_DECLARACAO_FREQUENCIA = "https://www.app.ueg.br/fenix-back-end/declaracao_frequencia/";
    String GERAR_NOVA_DECLARACAO_VINCULO = "https://www.app.ueg.br/fenix-back-end/declaracao_vinculo/";
    String DADOS_ATV_COMPLEMENTARES = "https://www.app.ueg.br/fenix-back-end/atividade_complementar/buscar_ac?acu_id=";
    String DADOS_ATV_COMPLEMENTARES_HORAS = "https://www.app.ueg.br/fenix-back-end/percurso_academico/evolucao_academica_atividade_complementar/?acu_id=";
    String DADOS_ATV_EXTENSAO = "https://www.app.ueg.br/fenix-back-end/percurso_academico/evolucao_academica_extensao/?acu_id=";
    String UEG_CONTATOS = "https://www.ueg.br/conteudo/22914_enderecos__telefones__e_mails_e_horario_de_atendimento";

    String GET_JWT_TOKEN = "https://www.app.ueg.br/auth/jwt/get?sistema=gestao_academica";
    String PRE_GERAR_NOVO_HISTORICO_ACADEMICO = "https://www.app.ueg.br/fenix-back-end/historico_academico/?jwt={0}";
    String GERAR_NOVO_HISTORICO_ACADEMICO = "https://www.app.ueg.br/fenix-back-end/historico_academico/nao_integralizado";


}
