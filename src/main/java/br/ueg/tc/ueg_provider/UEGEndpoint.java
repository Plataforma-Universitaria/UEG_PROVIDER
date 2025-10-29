package br.ueg.tc.ueg_provider;

public interface UEGEndpoint {

//    String VALIDA_LOGIN = "https://www.sistema.beta.ueg.br/auth/acesso/valida_login";
//    String ENTRA_PORTAL_ESTUDANTE = "https://www.sistema.beta.ueg.br/portal_estudante";
//    String ENTRA_PORTAL_PROFESSOR = "https://www.sistema.beta.ueg.br/portal_docente";
//    String PERFIL = "https://www.sistema.beta.ueg.br/fenix-back-end/perfil/";
//    String PERFIL_PROFESSOR = "https://www.sistema.beta.ueg.br/fenix-back-end/acesso/get_perfil_logado_no_portal_docente";
//    String DADOS_PROFESSOR = "https://www.sistema.beta.ueg.br/fenix-back-end/inicio/dados_docente";
//    String HORARIO_AULA = "https://www.sistema.beta.ueg.br/fenix-back-end/horario_aula";
//    String DADOS_DISCIPLINAS = "https://www.sistema.beta.ueg.br/fenix-back-end/percurso_academico_aluno/lista_matriculas_aluno/?acu_id=";
//    String DADOS_ACADEMICOS = "https://www.sistema.beta.ueg.br/fenix-back-end/perfil/dados_academicos";
//    String DADOS_ACADEMICOS_OBRIGATORIOS = "https://www.sistema.beta.ueg.br/fenix-back-end/perfil/dados_academicos";
//    String DADOS_ACADEMICOS_OPCIONAIS = "https://www.sistema.beta.ueg.br/fenix-back-end/perfil/dados_academicos";
//    String DADOS_ACADEMICOS_NUCLEO_LIVRE = "https://www.sistema.beta.ueg.br/fenix-back-end/perfil/dados_academicos";
//    String DADOS_ACADEMICOS_SEMINARIO = "https://www.sistema.beta.ueg.br/fenix-back-end/perfil/dados_academicos";
//    String GERAR_NOVA_DECLARACAO_FREQUENCIA = "https://www.sistema.beta.ueg.br/fenix-back-end/declaracao_frequencia/";
//    String GERAR_NOVA_DECLARACAO_VINCULO = "https://www.sistema.beta.ueg.br/fenix-back-end/declaracao_vinculo/";
//    String DADOS_ATV_COMPLEMENTARES = "https://www.sistema.beta.ueg.br/fenix-back-end/atividade_complementar_aluno/buscar_ac?acu_id=";
//    String DADOS_ATV_COMPLEMENTARES_HORAS = "https://www.sistema.beta.ueg.br/fenix-back-end/percurso_academico_aluno/evolucao_academica/?acu_id=";
//    String DADOS_ATV_EXTENSAO = "https://www.sistema.beta.ueg.br/fenix-back-end/percurso_academico_aluno/evolucao_academica/?acu_id=";
//    String UEG_CONTATOS = "https://www.ueg.br/conteudo/22914_enderecos__telefones__e_mails_e_horario_de_atendimento";
//    String GET_JWT_TOKEN = "https://www.sistema.beta.ueg.br/auth/jwt/get?sistema=gestao_academica";
//    String GET_JWT_TOKEN_PROFESSOR = "https://www.sistema.beta.ueg.br/auth//jwt/get?sistema=gestao_academica"; //Falar sobre as duas barras
//    String PRE_GERAR_NOVO_HISTORICO_ACADEMICO = "https://www.sistema.beta.ueg.br/fenix-back-end/historico_academico/?jwt={0}";
//    String GERAR_NOVO_HISTORICO_ACADEMICO = "https://www.sistema.beta.ueg.br/fenix-back-end/historico_academico/nao_integralizado";
//    String TC_ADICIONAR_ACOMPANHAMENTO = "https://www.sistema.beta.ueg.br/fenix-back-end/tc/adicionar_acompanhamento_tc";
//    String TC_EXCLUIR_ACOMPANHAMENTO = "https://www.sistema.beta.ueg.br/fenix-back-end/tc/remover_acompanhamento_tc";
//    String TC_BUSCAR_ACOMPANHAMENTO = "https://www.sistema.beta.ueg.br/fenix-back-end/tc/buscar_acompanhamento_tc?tcu_id=";
//    String LISTAR_TCS = "https://www.sistema.beta.ueg.br/fenix-back-end/tc/buscar_bancas_tc_lista";
//    String LISTAR_COMPONENTES = "https://www.sistema.beta.ueg.br/fenix-back-end/acesso/get_componentes_curriculares_ativos";
//    String LISTAR_PERIODOS = "https://www.app.ueg.br/fenix-back-end/inicio/get_periodos?depId=";
//    String LISTAR_CURSOS_POR_PERIODOS = "https://www.app.ueg.br/fenix-back-end/acesso/get_acesso?perfil=0&periodo=";

    String VALIDA_LOGIN = "https://www.app.ueg.br/auth/acesso/valida_login";
    String ENTRA_PORTAL_ESTUDANTE = "https://www.app.ueg.br/portal_estudante";
    String ENTRA_PORTAL_PROFESSOR = "https://www.app.ueg.br/portal_docente";
    String PERFIL = "https://www.app.ueg.br/fenix-back-end/perfil/";
    String HORARIO_AULA = "https://www.app.ueg.br/fenix-back-end/horario_aula";
    String DADOS_DISCIPLINAS = "https://www.app.ueg.br/fenix-back-end/percurso_academico_aluno/lista_matriculas_aluno/?acu_id=";
    String DADOS_ACADEMICOS = "https://www.app.ueg.br/fenix-back-end/perfil/dados_academicos";
    String DADOS_ACADEMICOS_OBRIGATORIOS = "https://www.app.ueg.br/fenix-back-end/perfil/dados_academicos";
    String DADOS_ACADEMICOS_OPCIONAIS = "https://www.app.ueg.br/fenix-back-end/perfil/dados_academicos";
    String DADOS_ACADEMICOS_NUCLEO_LIVRE = "https://www.app.ueg.br/fenix-back-end/perfil/dados_academicos";
    String DADOS_ACADEMICOS_SEMINARIO = "https://www.app.ueg.br/fenix-back-end/perfil/dados_academicos";
    String GERAR_NOVA_DECLARACAO_FREQUENCIA = "https://www.app.ueg.br/fenix-back-end/declaracao_frequencia/";
    String GERAR_NOVA_DECLARACAO_VINCULO = "https://www.app.ueg.br/fenix-back-end/declaracao_vinculo/";
    String DADOS_ATV_COMPLEMENTARES = "https://www.app.ueg.br/fenix-back-end/atividade_complementar_aluno/buscar_ac?acu_id=";
    String DADOS_ATV_COMPLEMENTARES_HORAS = "https://www.app.ueg.br/fenix-back-end/percurso_academico_aluno/evolucao_academica/?acu_id=";
    String DADOS_ATV_EXTENSAO = "https://www.app.ueg.br/fenix-back-end/percurso_academico_aluno/evolucao_academica/?acu_id=";
    String UEG_CONTATOS = "https://www.ueg.br/conteudo/22914_enderecos__telefones__e_mails_e_horario_de_atendimento";
    String GET_JWT_TOKEN = "https://www.app.ueg.br/auth/jwt/get?sistema=gestao_academica";
    String PRE_GERAR_NOVO_HISTORICO_ACADEMICO = "https://www.app.ueg.br/fenix-back-end/historico_academico/?jwt={0}";
    String GERAR_NOVO_HISTORICO_ACADEMICO = "https://www.app.ueg.br/fenix-back-end/historico_academico/nao_integralizado";
    String GET_JWT_TOKEN_PROFESSOR = "https://www.app.ueg.br/auth//jwt/get?sistema=gestao_academica";
    String TC_ADICIONAR_ACOMPANHAMENTO = "https://www.app.ueg.br/fenix-back-end/tc/adicionar_acompanhamento_tc";
    String TC_EXCLUIR_ACOMPANHAMENTO = "https://www.app.ueg.br/fenix-back-end/tc/remover_acompanhamento_tc";
    String TC_BUSCAR_ACOMPANHAMENTO = "https://www.app.ueg.br/fenix-back-end/tc/buscar_acompanhamento_tc?tcu_id=";
    String LISTAR_TCS = "https://www.app.ueg.br/fenix-back-end/tc/buscar_bancas_tc_lista";
    String LISTAR_COMPONENTES = "https://www.app.ueg.br/fenix-back-end/acesso/get_componentes_curriculares_ativos";
    String LISTAR_PERIODOS = "https://www.app.ueg.br/fenix-back-end/inicio/get_periodos?depId=";
    String LISTAR_CURSOS_POR_PERIODOS = "https://www.app.ueg.br/fenix-back-end/acesso/get_acesso?perfil=0&periodo=";
    String PERFIL_PROFESSOR = "https://www.app.ueg.br/fenix-back-end/acesso/get_perfil_logado_no_portal_docente";
    String DADOS_PROFESSOR = "https://www.app.ueg.br/fenix-back-end/inicio/dados_docente";

}
