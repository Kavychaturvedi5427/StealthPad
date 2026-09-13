package com.kavya.stealthpad.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import com.kavya.stealthpad.Dto.AiGenerateResDto;
import com.kavya.stealthpad.Dto.AiKeyPointsResDto;
import com.kavya.stealthpad.Dto.AiSummarizeResDto;

@Service

public class AiService {

        private final ChatClient chatClient;

        public AiService(ChatClient.Builder chatBuilder) {
                this.chatClient = chatBuilder.build();
        }

        // Method to summarize the note...
        public AiSummarizeResDto summarize(String text) {
                String response = chatClient
                                .prompt()
                                // system level prompt...
                                .system("""
                                                You are an AI assistant for StealthPad, a private
                                                note-taking application.

                                                Summarize the user's note in 2-4 concise sentences.
                                                Keep only the most important information.
                                                Preserve important facts and intentions.
                                                Do not add information that is not present in the note.
                                                Return only the summary.
                                                """)
                                // user level prompt...
                                .user("""
                                                Summarize the following note:

                                                %s
                                                """.formatted(text))
                                .call()
                                .content();

                // converting the response into AiResponseDto...
                AiSummarizeResDto dto = new AiSummarizeResDto();
                dto.setSummary(response);
                dto.setGeneratedAt(LocalDateTime.now());

                return dto;
        }

        // method to generate the note...
        public AiGenerateResDto generateNote(String text) {

                String generatedNote = chatClient
                                .prompt()
                                .system("""
                                                You are Stealth AI, the writing assistant for StealthPad,
                                                a private note-taking application.

                                                Your job is to turn the user's input into useful,
                                                detailed, well-structured notes.

                                                Follow these rules:

                                                1. If the user provides a topic, keyword, or short phrase,
                                                   develop it into a meaningful and informative note.

                                                2. If the user provides rough thoughts, keywords,
                                                   or incomplete sentences, transform them into
                                                   clear and organized notes.

                                                3. If the user provides a question or instruction,
                                                   directly fulfill that request.

                                                4. Do not ask the user for clarification unless the
                                                   request is genuinely impossible to answer.

                                                5. Preserve the user's intended meaning.

                                                6. Do not invent personal facts or information about
                                                   the user.

                                                7. Do not give an extremely short response.
                                                   Provide enough explanation to make the note useful.

                                                8. For educational or technical topics, explain the
                                                   important concepts clearly and include examples
                                                   where appropriate.

                                                9. Use a clear structure with:
                                                   - A descriptive title
                                                   - Headings or subheadings when useful
                                                   - Bullet points or numbered lists where appropriate
                                                   - Examples when they improve understanding

                                                10. Aim for approximately 200-500 words when the topic
                                                    allows it. Use less when the user's request is
                                                    naturally short, and more when additional detail
                                                    is genuinely useful.

                                                11. Stay focused on the user's request.
                                                    Do not add unnecessary filler.

                                                12. Return only the generated note content.
                                                Do not include explanations about these instructions.
                                                """)
                                .user("""
                                                Create a useful, structured note about the following input:

                                                %s
                                                """.formatted(text))
                                .call()
                                .content();

                AiGenerateResDto resDto = new AiGenerateResDto();
                resDto.setGeneratedNote(generatedNote);
                resDto.setGeneratedAt(LocalDateTime.now());

                return resDto;
        }

        // Key point extraction...
        public AiKeyPointsResDto extractKeyPoints(String text) {
                List<String> keyPoints = chatClient
                                .prompt()
                                .system("""
                                                You are an AI assistant for StealthPad,
                                                a private note-taking application.

                                                Extract the most important points from the user's note.

                                                Rules:
                                                - Return 3 to 7 key points.
                                                - Each point must be concise.
                                                - Preserve important facts, decisions, tasks,
                                                  and intentions.
                                                - Do not add information that is not present.
                                                - Do not include an introduction or conclusion.
                                                """)
                                .user("""
                                                Extract the key points from this note:

                                                %s
                                                """.formatted(text))
                                .call()
                                .entity(new ParameterizedTypeReference<List<String>>() {
                                });
                AiKeyPointsResDto resDto = new AiKeyPointsResDto();
                resDto.setKeyPoints(keyPoints);
                resDto.setGeneratedAt(LocalDateTime.now());

                return resDto;

        }

}
