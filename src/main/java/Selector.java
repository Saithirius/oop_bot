import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

class Selector {
    private static final Logger log = Bot.log;
    private static DatabaseController controller = TelegramBot.controller;

    static Answer getAnswer(User user, String request){
        FiniteStateMachine.State state = user.FSM.workWithRequest(request);
        switch (state) {
            case SayHello:
                log.config("FSM - SayHello");
                return new Answer(new String[]{"Привет, я Киноман!\nМогу помочь с выбором фильма, сериала и т.д.\n" +
                        "Для справки введи \"помощь\""},
                        new String[]{"Помощь", "Возможные запросы"}, new String[0]);

            case Requesting:
                log.config("FSM - Requesting");
                user.kinoman = Kinoman.createKinomanOnRequest(user.id, request);
                Movie movie = user.kinoman.getNext();
                if (movie == null){
                    return new Answer(new String[]{"Ты пытался найди хоррор мультик или что-то вроде?" +
                            "\nВ любом случае я ничего не нашел."}, new String[0], new String[0]);
                }
                return new Answer(new String[]{movie.toStringMovie()},
                        new String[]{"Похожие", "Следующий"}, new String[]{"Скрыть", "Аннотация"});

            case ShowingAnnotation:
                log.config("FSM - ShowingAnnotation");
                return new Answer(new String[]{user.kinoman.currentMovie.getAnnotation()},
                        new String[]{"Скрыть", "Следующий"},  new String[]{"Похожие"});

            case ShowingSimilar:
                log.config("FSM - ShowingSimilar");
                List<Movie> similarMovie = user.kinoman.showSimilar();
                if(similarMovie != null) {
                    ArrayList<String> answer = new ArrayList<String>() {
                    };
                    for (Movie curMovie: similarMovie){
                        answer.add(curMovie.toStringMovie());
                    }
                    return new Answer(answer.toArray(new String[0]),
                            new String[]{"Следующий"}, new String[0]);
                } else {
                    return new Answer(new String[]{"Я не нашел похожих, сорян"},
                            new String[]{"Следующий"}, new String[0]);
                }

            case ShowingNext:
                log.config("FSM - ShowingNext");
                movie = user.kinoman.getNext();
                if (movie == null){
                    return new Answer(new String[]{"Это был последний"}, new String[0], new String[0]);
                }
                return new Answer(new String[]{movie.toStringMovie()},
                        new String[]{"Похожие", "Следующий"}, new String[]{"Скрыть", "Аннотация"});

            case BanningCurrentRequest:
                log.config("FSM - BanningRequest");
                String movie_n = user.kinoman.currentMovie.name;
                controller.addMovieToBlackList(user.id, movie_n);
                return new Answer(new String[]{"Больше я не буду показывать вам - "+movie_n},
                        new String[]{"Похожие", "Следующий"}, new String[0]);

            case ShowingHelp:
                log.config("FSM - ShowingNext");
                return new Answer(new String[]{Kinoman.printHelp()},
                        new String[]{"Возможные запросы"}, new String[0]);

            case ShowingValidRequests:
                log.config("FSM - ShowingNext");
                return new Answer(new String[]{Kinoman.printValidRequest()}, new String[0], new String[0]);

            default:
                return new Answer(new String[]{"Некорректная команда" +
                        "\nМожет ты что-то не правильно написал?"},
                        new String[]{"Помощь", "Возможные запросы"}, new String[0]);
        }
    }
}
