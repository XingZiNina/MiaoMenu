# MiaoMenu

## Feature Coverage Matrix

| Issue #5 Requirement | Current Implementation |
| --- | --- |
| item_conditions | Supported for Java/Bedrock menu item-level conditions |
| view_requirement | Supported for menu-level access requirements |
| requirement_blocks | Supported as reusable requirement_blocks |
| deny_message_and_fallback_ui | Supported via deny_message and fallback_menu |
| Placeholder support | Preserved and extended with PlaceholderAPI/basic placeholder parsing |
| Multi-language messages | Unified through the messages section in config.yml |
| Permission nodes | Supported through permission requirements and plugin.yml permissions |
| Data persistence | Still YAML-based configuration persistence |

## Build

```bash
mvn test
mvn package
```

Generated artifact: `target/MiaoMenu-2.7.7.9.jar`
