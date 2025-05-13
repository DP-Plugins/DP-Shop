<details>
	<summary>korean</summary>
	
# DP-Shop 플러그인 소개

DP-Shop은 마인크래프트 서버에서 상점을 쉽게 생성하고 관리할 수 있는 플러그인입니다.
GUI를 통해 직관적으로 아이템과 가격을 설정할 수 있으며, 상점의 활성화/비활성화 및 페이징 기능을 지원합니다.

## 플러그인 특징
- **GUI 기반 설정**: 아이템과 가격을 GUI로 간편하게 설정 가능.
- **상점 활성화/비활성화**: 특정 상점을 필요에 따라 활성화하거나 비활성화 가능.
- **페이징 기능**: 상점을 여러 페이지로 나누어 관리 가능 (페이지는 0부터 시작).
- **권한 설정**: 상점별 접근 권한을 설정하거나 삭제 가능.
- **DLang 지원**: 언어 파일을 자유롭게 수정하실 수 있습니다.

## 명령어
| 명령어 | 설명 |
|--------|------|
| `/dshop create <name>` | 새로운 상점을 생성합니다. |
| `/dshop title <name> <title>` | 상점의 제목을 설정합니다. |
| `/dshop pages <name> <maxPage>` | 상점의 최대 페이지를 설정합니다. (페이지는 0부터 시작) |
| `/dshop items <name> [page]` | 아이템 설정 GUI를 엽니다. |
| `/dshop price <name> [page]` | 가격 설정 GUI를 엽니다. |
| `/dshop enable <name>` | 상점을 활성화합니다. |
| `/dshop disable <name>` | 상점을 비활성화합니다. |
| `/dshop delete <name>` | 상점을 삭제합니다. |
| `/dshop reload` | 설정 파일을 다시 불러옵니다. |
| `/dshop permission <name> <node>` | 상점에 권한을 설정합니다. |
| `/dshop delpermission <name>` | 상점의 권한을 삭제합니다. |
| `/dshop open <name>` | 상점을 엽니다. (유저도 사용 가능) |

## 사용법 예시
- 상점 생성: `/dshop create myshop`
- 상점 페이지 설정: `/dshop pages myshop 3`
- 아이템 설정 GUI 열기: `/dshop items myshop 0`
- 상점 열기: `/dshop open myshop`
</details>

<details>
	<summary>english</summary>
	
# DP-Shop Plugin Introduction

DP-Shop is a Minecraft plugin that allows for easy creation and management of shops on servers. It offers intuitive item and price configuration through a GUI, along with features for enabling/disabling shops and pagination.

## Plugin Features
- **GUI-Based Configuration**: Easily set items and prices using a graphical interface.
- **Shop Enable/Disable**: Activate or deactivate specific shops as needed.
- **Pagination**: Organize shops across multiple pages (pages start from 0).
- **Permission Settings**: Set or remove access permissions for individual shops.
- **DLang Support**: You can freely edit language files.

## Commands
| Command | Description |
|---------|-------------|
| `/dshop create <name>` | Creates a new shop. |
| `/dshop title <name> <title>` | Sets the title of a shop. |
| `/dshop pages <name> <maxPage>` | Sets the maximum number of pages for a shop (pages start from 0). |
| `/dshop items <name> [page]` | Opens the item configuration GUI. |
| `/dshop price <name> [page]` | Opens the price configuration GUI. |
| `/dshop enable <name>` | Enables a shop. |
| `/dshop disable <name>` | Disables a shop. |
| `/dshop delete <name>` | Deletes a shop. |
| `/dshop reload` | Reloads the configuration file. |
| `/dshop permission <name> <node>` | Sets a permission for a shop. |
| `/dshop delpermission <name>` | Removes a permission from a shop. |
| `/dshop open <name>` | Opens a shop (usable by players). |

## Usage Examples
- Create a shop: `/dshop create myshop`
- Set shop pages: `/dshop pages myshop 3`
- Open item configuration GUI: `/dshop items myshop 0`
- Open a shop: `/dshop open myshop`
</details>
