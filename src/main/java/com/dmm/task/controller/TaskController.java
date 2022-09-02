package com.dmm.task.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;

import com.dmm.task.Form.TaskForm;
import com.dmm.task.entity.Tasks;
import com.dmm.task.repository.TasksRepository;
import com.dmm.task.service.AccountUserDetails;

@Controller
public class TaskController {

	@Autowired
	private TasksRepository tasks_repo;

	@GetMapping("/main")
	public String main(Model model) {

		// ① 2次元表になるので、ListのListを用意する
		List<List<LocalDate>> matrix = new ArrayList<>();

		// ② 1週間分のLocalDateを格納するListを用意する
		List<LocalDate> week_1 = new ArrayList<>();

		// ③ その月の1日のLocalDateを取得する
		LocalDate day = LocalDate.now();
		day = LocalDate.of(day.getYear(), day.getMonthValue(), 1);

		// ④ 曜日を表すDayOfWeekを取得し
		DayOfWeek DayOfWeek_1 = day.getDayOfWeek();

		// ④ 上で取得したLocalDateに曜日の値（DayOfWeek#getValue)をマイナスして前月分のLocalDateを求める
		// https://qiita.com/tora_kouno/items/d230f904a2b768ccb319
		// http://gucci1208.com/2%E3%81%A4%E3%81%AEcalendar%E3%82%AF%E3%83%A9%E3%82%B9%E3%82%92%E6%AF%94%E8%BC%83%E3%81%97%E3%81%A6%E3%80%81%E6%97%A5%E6%95%B0%E5%B7%AE%E3%82%92%E5%8F%96%E5%BE%97%E3%81%99%E3%82%8B-488.html
		// http://heppoen.seesaa.net/article/480642483.html
		day = day.minusDays(DayOfWeek_1.getValue());

		// ⑤ 1日ずつ増やしてLocalDateを求めていき、2．で作成したListへ格納していき、1週間分詰めたら1．のリストへ格納する

		for (int i = 1; i <= 7; i++) {
			// 1日ごとにdayをweekにaddしなければいけない
			week_1.add(day);
			// 1日増やす（dayをプラス1日する）
			day.plusDays(1);
		}
		matrix.add(week_1);

		// ⑥2週目以降は単純に1日ずつ日を増やしながらLocalDateを求めてListへ格納していき、土曜日になったら1．のリストへ格納して新しいListを生成する（月末を求めるにはLocalDate#lengthOfMonth()を使う）
		List<LocalDate> week_2 = new ArrayList<>();
		List<LocalDate> week_3 = new ArrayList<>();
		List<LocalDate> week_4 = new ArrayList<>();
		List<LocalDate> week_5 = new ArrayList<>();

		for (int i = 7; i <= day.lengthOfMonth(); i++) {
			int d = 2;
			int w = 2;

			if (day.getDayOfWeek() == java.time.DayOfWeek.SATURDAY) {
				switch (w) {
				case 2:
					matrix.add(week_2);
					break;
				case 3:
					matrix.add(week_3);
					break;
				case 4:
					matrix.add(week_4);
					break;

				w++;
				d++;
			}

			// 1日ごとにdayをweekにaddしなければいけない
			switch (d) {
			case 2:
				week_2.add(day);
				break;
			case 3:
				week_3.add(day);
				break;
			case 4:
				week_4.add(day);
				break;
			}

			// 1日増やす（dayをプラス1日する）
			day = day.plusDays(1);
		}

		// ⑦ 最終週の翌月分をDayOfWeekの値を使って計算し、
		for (int j = 1; j <= 7 - day.getDayOfWeek().getValue(); j++) {
			// 1日増やす（dayをプラス1日する）
			day = day.plusDays(1);
			week_5.add(day);
		}

		// ⑦ 6．で生成したリストへ格納し、最後に1．で生成したリストへ格納する
		matrix.add(week_5);

		List<Tasks> list = tasks_repo.findAll();
		model.addAttribute("tasks", list);
		TaskForm taskForm = new TaskForm();
		model.addAttribute("taskForm", taskForm);

		return "/main";
	}

	@GetMapping("/")
	public String index() {
		return "/login";
	}

	@GetMapping("/main/create")
	public String create(@Validated TaskForm taskForm, BindingResult bindingResult,
			@AuthenticationPrincipal AccountUserDetails user, Model model) {

		if (bindingResult.hasErrors()) {
			// エラーがある場合は投稿登録画面を返す
			List<Tasks> list = tasks_repo.findAll(Collections.sort("date"));
			model.addAttribute("tasks", list);
			model.addAttribute("taskForm", taskForm);
			return "/main/create";
		}

		Tasks task = new Tasks();
		task.setName(user.getName());
		task.setTitle(task.getTitle());
		task.setText(task.getText());
		task.setDate(LocalDateTime.now());

		tasks_repo.save(task);

		return "/redirect:";
	}

	@GetMapping("/main/edit/{id}")
	public String main_edit_id() {
		return "/redirect:";
	}

	@GetMapping("/login")
	public String login() {
		return "login";
	}

	@GetMapping("/logout")
	public String logout() {
		return "login";
	}
}
