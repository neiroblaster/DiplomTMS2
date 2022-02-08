package tms.shchayuk.audition.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import tms.shchayuk.audition.entity.Line;
import tms.shchayuk.audition.entity.Song;
import tms.shchayuk.audition.entity.Word;
import tms.shchayuk.audition.service.interfaces.LineService;
import tms.shchayuk.audition.service.interfaces.SongService;
import tms.shchayuk.audition.service.interfaces.WordService;
import tms.shchayuk.audition.utility.Answers;
import tms.shchayuk.audition.utility.AnswersFromClient;
import tms.shchayuk.audition.utility.AnswersFromDAO;
import tms.shchayuk.audition.utility.Strategy;

import java.util.List;
import java.util.Locale;

@Controller
public class SongController {

    @Autowired
    SongService songService;

    @Autowired
    Strategy strategy;

    @Autowired
    LineService lineService;

    @Autowired
    WordService wordService;

    @Autowired
    private AnswersFromDAO answersFromDAO;

    @Autowired
    private AnswersFromClient answersFromClient;

    @Autowired
    private Answers answers;

    @GetMapping("/showAllSongs")
    public String showAllSongs(Model model) {
        model.addAttribute("allSongs", songService.findAll());

        return "all-songs";
    }

    @GetMapping("/addNewSong")
    public String addNewSong(Model model) {
        Song song = new Song();
        model.addAttribute("song", song);
        return "song-info";
    }

    @PostMapping("/saveSong")
    public String saveSong(@ModelAttribute("song") Song song) {
        songService.saveSong(song);
        strategy.splitAndSaveWordsBySongId(song.getId());
        return "redirect:/showAllSongs";
    }

    @GetMapping("/deleteSong/{id}")
    public String deleteSongById(@PathVariable("id") int id) {
        songService.deleteSongById(id);
        return "redirect:/showAllSongs";
    }

    @GetMapping("/showSong")
    public String showSong(@RequestParam("songId") int id, Model model) {

        List<Line> allLines = lineService.findAllBySongId(id);
        List<Integer> linesId = lineService.getLinesIdBySongId(id);
        List<Word> allWords = wordService.getWordsBySlineId(linesId);

        strategy.doStrategy(allWords, allLines);

        AnswersFromClient answersFromClient = new AnswersFromClient();

        model.addAttribute("allLines", allLines);
        model.addAttribute("allWords", allWords);
        model.addAttribute(answersFromDAO);
        model.addAttribute(answersFromClient);

        return "show-song";
    }

    @RequestMapping("/checkAnswers")
    public String checkAnswers(@ModelAttribute AnswersFromClient answersFromClient,
                               Model model) {
        int id = answers.getAnswerList().get(0).getSongId();
        List<Line> allLines = lineService.findAllBySongId(id);
        List<Integer> linesId = lineService.getLinesIdBySongId(id);
        List<Word> allWords = wordService.getWordsBySlineId(linesId);

        model.addAttribute("allLines", allLines);
        model.addAttribute("allWords", allWords);

        List<String> aFClient = answersFromClient.getAnswersFromClient();
        int numberOfRightAnswers = 0;
        for (int i = 0; i < aFClient.size(); i++) {
            answers.getAnswerList().get(i).setClientAnswer(aFClient.get(i));
            if (answers.getAnswerList().get(i).getRightAnswer().toLowerCase(Locale.ROOT).equals(aFClient.get(i).toLowerCase(Locale.ROOT))) {
                answers.getAnswerList().get(i).setCheck(true);
                numberOfRightAnswers++;
            } else {
                answers.getAnswerList().get(i).setCheck(false);
            }
        }
        model.addAttribute("answers", answers);
        model.addAttribute("numberOfRightAnswers", numberOfRightAnswers);

        return "show-answers";
    }
}
