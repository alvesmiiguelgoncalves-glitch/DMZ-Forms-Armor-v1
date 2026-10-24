# DMZ SSJ4 Armors

Addon do **DragonMine Z** (Forge 1.20.1) que troca a textura das armaduras do mod quando o jogador está em:

- **Super Saiyajin 4** — tanto o do GT quanto o do Daima
- **Super Saiyajin Full Power** (a forma de cabelo verde do Broly)

A troca acontece com uma **transição suave** (fade) entre a roupa normal e a da forma, no mesmo ritmo da pelagem do SSJ4 do DMZ. Funciona no corpo, na mão em primeira pessoa e nas capas.

## Como compilar pelo GitHub

1. Suba esta pasta inteira para um repositório seu no GitHub.
2. `gradle.properties` já aponta para o DragonMine Z **v2.2** no CurseForge (`dmz_curse_file_id=9282248`). Para compilar contra outra versão, abra a [página de arquivos do DragonMine Z](https://www.curseforge.com/minecraft/mc-mods/dragonminez/files/all) e troque esse valor pelo **File ID** do arquivo desejado.
3. Faça o commit. A aba **Actions** roda o build sozinha; o `.jar` fica em **Artifacts → dmz-ssj4-armors**.
4. Coloque o `.jar` na pasta `mods` junto com o DragonMine Z.

Se o build falhar, o motivo aparece no log da Action. Uma causa provável é uma mudança no código do DMZ entre a versão 2.2 e a que você usar.

## Configuração

Depois de abrir o jogo uma vez, edite `config/dmz_ssj4_armors-client.toml`:

| Opção | Padrão | O que faz |
|---|---|---|
| `enabled` | `true` | Liga/desliga o addon |
| `fadeTicks` | `10` | Duração da transição (20 ticks = 1 s) |
| `forms` | `oozaru:supersaiyan4`, `supersaiyan:supersaiyan4`, `legendaryforms:ssjfullpower` | Formas no formato `grupo:forma` que ativam a roupa alternativa |

SSJ4 (GT e Daima) também é detectado pelo modelo da forma (`ssj4gt` / `ssj4d`), igual ao que o DMZ usa para a pelagem. Para adicionar outra forma, basta incluir `grupo:forma` na lista.

## Como funciona

- As texturas alternativas ficam em `src/main/resources/assets/dmz_ssj4_armors/textures/armor/`, **com o mesmo nome** das do DMZ (`goku_gi_layer1.png`, `goku_gi_damaged_layer1.png`, ...).
- Um mixin no `ArmorTextureResolver` do DMZ olha qual textura ele escolheu e, se o jogador está numa forma-alvo e existe uma versão alternativa com o mesmo nome, devolve a alternativa. Se não existe, a roupa normal é usada, então nada quebra.
- Na transição, o addon gera texturas intermediárias (mistura das duas em 8 passos) e usa a do passo atual.
- Mixins usados: `ArmorTextureResolver`, `DbzArmorItem` / `DbzArmorPatrollerItem` / `DbzArmorCapeItem`, `DMZCustomArmorLayer`, `DMZCapeLayer`, `DMZRenderHand`.

Para trocar ou adicionar uma roupa: coloque o PNG na pasta acima com o nome exato da textura original do DMZ.

## Limitações conhecidas

- A transição começa quando o DMZ muda a forma ativa do jogador, não durante a animação de carga.
- Pixels que só existem numa das duas versões da roupa aparecem/somem durante o fade, em vez de escurecer.
- As mãos em primeira pessoa e as capas dependem de métodos internos do DMZ (`renderDbzArmor` e `DMZCapeLayer.render`). Se uma versão futura do DMZ mudar isso, só essas duas partes deixam de trocar.
- Este projeto foi escrito lendo o código do DMZ 2.2 e **ainda não foi testado dentro do jogo**.

## Créditos e licença

- As texturas são do texturepack **"SSJ4 Armors", por Miiguel765Gamer**. Antes de publicar este addon (CurseForge, Modrinth etc.), confirme com ele que a redistribuição é permitida e mantenha o crédito.
- DragonMine Z é de seus autores (licença GPL v3 no repositório oficial). Este addon não inclui código nem texturas do DMZ.
- Defina uma licença para o **código** do addon (por exemplo MIT) e atualize `mod_license` e `mod_authors` em `gradle.properties`.
