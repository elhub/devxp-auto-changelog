package no.elhub.devxp.autochangelog

fun createMockResponse(key: String, title: String, body: String): String = """
        {
            "expand": "renderedFields,names,schema,operations,editmeta,changelog,versionedRepresentations",
            "id": "1",
            "self": "https://google.atlassian.net/rest/api/3/issue/1",
            "key": "$key",
            "fields": {
                "summary": "$title",
                "description": {
                    "type": "doc",
                    "version": 1,
                    "content": [
                        {
                            "type": "paragraph",
                            "content": [
                                {
                                    "type": "text",
                                    "text": "$body"
                                }
                            ]
                        }
                    ]
                }
            }
        }
""".trimIndent()
