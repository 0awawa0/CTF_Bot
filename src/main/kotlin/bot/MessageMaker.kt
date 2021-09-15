package bot

import bot.Bot.Companion.DATA_COMMANDS
import bot.Bot.Companion.DATA_CURRENT_SCOREBOARD
import bot.Bot.Companion.DATA_FILE
import bot.Bot.Companion.DATA_GLOBAL_SCOREBOARD
import bot.Bot.Companion.DATA_MENU
import bot.Bot.Companion.DATA_TASKS
import bot.features.magic.MagicNumbers
import bot.features.numbers.NumbersUtils
import bot.features.rot.Rot
import bot.utils.Helper
import database.DbHelper
import database.PlayerModel
import org.telegram.telegrambots.meta.api.methods.send.SendDocument
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendSticker
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import utils.Logger
import java.io.File
import java.lang.ref.WeakReference


class MessageMaker(private val bot: WeakReference<Bot>) {

    private val tag = "MessageMaker"

    //This chars must be escaped in markdown
    //'_', '*', '[', ']', '(', ')', '~', '`', '>', '#', '+', '-', '=', '|', '{', '}', '.', '!'

    private val allowedCharacters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".toSet() +
            "абвгдеёжзийклмнопрстуфхцчшщъыьэюяАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ_-.".toSet()

    private val niceStickers = setOf(
        "CAACAgIAAxkBAAI4yGDbE1494ffZg_wSh7-dsXYWsgdYAALVBwACRvusBEqXUnkJhF84IAQ",
        "CAACAgIAAxkBAAI4xmDbExVeAAENLPrCCFtnb7x6_AfWHwAC6gcAAkb7rASUHp5Nfp3HaCAE",
        "CAACAgIAAxkBAAI4tmDbEJeTJHFiriwRRhQ3EAaukdLlAAJ0HwACooqWASrB2U8ghGu0IAQ",
        "CAACAgIAAxkBAAIJT18VnLcHUgsFzCcxHihdJ1AowVFMAAKWAAMfAUwVQT-DgR0Ym0saBA",
        "CAACAgIAAxkBAAIJTV8VnK9HKA94FtdBFWjzmCu-Uwn6AALtBwACYyviCSNLVcKzULbEGgQ",
        "CAACAgIAAxkBAAIJWl8WfpblDWCMQptHaCcoMNvqOlqSAAJ0AQACihKqDqn6Ep_umnvhGgQ",
        "CAACAgIAAxkBAAIJXl8WfsLYOjRW-7UltRYl9Dmm9oI2AAL-AANWnb0K2gRhMC751_8aBA",
        "CAACAgIAAxkBAAI4v2DbEcvXnBCp56JkShgOUQV9vevEAAKAAAPBnGAMNSI9fXm2854gBA",
        "CAACAgIAAxkBAAIJSV8VnFYVJRnEPBZhS3Eu7dWUD5QvAALzAgACnNbnCuAuBHGFD8ECGgQ",
        "CAACAgEAAxkBAAI4xGDbEwhN2UWqNCHWKkrrp_5-YNJdAAL_AAM4DoIRy3vWd2ul3nUgBA",
        "CAACAgUAAxkBAAIJVF8VnN7sbq-xIlubyOfF3yTtF4UuAAKsAwAC6QrIA7C0qC7bh6c7GgQ",
        "CAACAgIAAxkBAAIJjl8Wh_BJYm0aNMft2fD_56Q6QKfiAAImAwACz7vUDqRT7fQiGuLvGgQ",
        "CAACAgIAAxkBAAIJSl8VnFo5nBLmgFhi_cM6efEShZqVAAL5BwACGELuCAh1fKDO8HNOGgQ",
        "CAACAgIAAxkBAAIJW18Wfp6MbnlP03waFSGOxGKGwftIAAL9CQACLw_wBrlfDQPsjDttGgQ",
        "CAACAgIAAxkBAAIJjV8Wh-yWKz_qWTIZcOQ8LW6Hsk_NAAKVAAMfAUwVAfFU0Ca-WLcaBA",
        "CAACAgIAAxkBAAI4vmDbEU_vWonIo75MrGIZoIdPSYnLAAJ4AAMQIQIQVgLTdGip2AABIAQ",
        "CAACAgIAAxkBAAIJUl8VnMzHCA0COXVkf7xzgu3A2vBRAAIyCgACbjLYAAH9b7brkPA7TRoE",
        "CAACAgIAAxkBAAIJXF8WfqVcu0_TGUjvTnom-2f6FWbJAAK0AgACW__yCmCB33V_fjhuGgQ",
        "CAACAgIAAxkBAAI4zmDbE_s7jGGYXzaWx2Q-lZWeE7bPAAIsAANEDc8XyPvc7VqXDYsgBA",
        "CAACAgIAAxkBAAIJTl8VnLR1FJnJkBI0VecVVe1qo5o7AALpBwACYyviCXvSqf4U1mKMGgQ",
        "CAACAgIAAxkBAAIJXV8WfqrBeL4l4rCKeOE4g7D5czdEAALoAgACtXHaBlINrrZVgIJbGgQ",
        "CAACAgIAAxkBAAIJYF8WftVOWc6aFz_hTTVxJgggCjVXAAIgAAOWn4wOrP1BM_Sqb_kaBA",
        "CAACAgIAAxkBAAI4zGDbE5ogLfwK3BzyoakubDAPVrBjAAI4AAN4qOYP_tlS9QdPmhkgBA",
        "CAACAgIAAxkBAAI4y2DbE4haCFuYUvi9o4svpB-kYkT9AAJBAAN4qOYP-J7xorhFu34gBA",
        "CAACAgIAAxkBAAI40GDbFCgX_YizNiNC8JjUOrKw1jxlAAKmAQAC8UK_BfPqOPDovILxIAQ",
        "CAACAgIAAxkBAAIJk18WiD-wYWHHWJ0Uqt6t0jxK-WJ6AAJlAwAC7sShCkZUXth8bywsGgQ",
        "CAACAgIAAxkBAAI4w2DbEv1ZKjVrDfuWHzyKfwydMFQeAAIBAQACIjeOBE2NeYSdvlefIAQ",
        "CAACAgIAAxkBAAIJkF8WiATgl_sq4wZXUP-mgKMTDBGmAAIfAAMNttIZUwyqkRgWjiAaBA",
        "CAACAgIAAxkBAAIJUF8VnLvWZA19VDS207QfeRlOKbdoAAJXCQACeVziCSskAcAPGVVeGgQ",
        "CAACAgIAAxkBAAI4wGDbErAhitYis-TFoRmWWXbyCCZuAALPHwACooqWAfznXLGCKZZmIAQ",
        "CAACAgIAAxkBAAIJWF8WfndKRVEEPtHafWczBHRjcnFXAAI2AgACz7vUDoh3s73RN0lHGgQ",
        "CAACAgIAAxkBAAIJTF8VnKeHcCXNyXpeD8gwX2_jZJ7EAAIbAQACYyviCRVFtTZcAq6uGgQ",
        "CAACAgIAAxkBAAI4z2DbFB7d___76y12BW_Ku_VSwbqeAAIeAgACNnYgDluSTg2uvsW3IAQ",
        "CAACAgIAAxkBAAIJX18WfshmsxFruyhfiwasTdWvcx67AAL8AAMw1J0RU6XxJu0oNegaBA",
        "CAACAgIAAxkBAAI4ymDbE3g5kxcrBk2CkNcG-f-RfLb2AAIxAwACbbBCA5qcE5gargaAIAQ",
        "CAACAgIAAxkBAAI4yWDbE2l3fA7ILO_iON-nu_IvmQ8_AALJAQACVp29CnXYcMSIGS6NIAQ",
        "CAACAgIAAxkBAAIJjF8Wh-UNsbzRQratjVJbM4qjDe3xAALkAAPEe4QKEcIHSKqKDJQaBA",
        "CAACAgIAAxkBAAII818ViQ806_Vkg6bol8ALkVEPOPBIAAIlAAM7YCQUglfAqB1EIS0aBA",
        "CAACAgUAAxkBAAIJWV8WfoN1skrCCtKeQc2jiZH04zQaAAKHAwAC6QrIAypdTyYxR1EwGgQ",
        "CAACAgIAAxkBAAIJVl8VnO7R91RmoyvPanX0cuE_9QQNAAJGAANSiZEj-P7l5ArVCh0aBA",
        "CAACAgUAAxkBAAIJVV8VnON2NIhUaBgnHyr4bY0Q-txhAAJuAwAC6QrIA3w0Dz_a7ARtGgQ",
        "CAACAgIAAxkBAAI4xWDbEw6Us-Ek85pVcvZIrEblNl3PAAIbCQACGELuCNy5pdXzSq7IIAQ",
        "CAACAgIAAxkBAAIJSF8VnFK_9gEmaVBryUw9QPXdIC1VAAINAgACNnYgDjJnEuNd-1iCGgQ",
        "CAACAgIAAxkBAAIJkV8WiA0MQOITIucEncqGtw6VDAN9AAJrAAOWn4wOMuKqiPbhniUaBA",
        "CAACAgIAAxkBAAIJR18VnEnsspEr1-c0nfuivqrsLF_4AAJ4CQAC8UK_BcyW4BnRNuwKGgQ",
        "CAACAgIAAxkBAAIJYV8WfvQNewABa1o6xvFd_15l57iZQAACdAMAAvoLtgjV5oYGGtDaUBoE",
        "CAACAgUAAxkBAAI4wWDbErehRd1B60I5eEjZpXqA3BC3AALyAgACra44V5u5bJqDYtoGIAQ",
        "CAACAgIAAxkBAAI4uWDbENUqOp_WAAFz6JPr8xAMMuodwAACCQADjrKQEzqpFR9Ms2mDIAQ",
        "CAACAgIAAxkBAAIJi18Wh9_7jkge3HN8iqY8k2f8xNT6AAIiAgACNnYgDjgrKIrs7Ue3GgQ",
        "CAACAgIAAxkBAAIJUV8VnMfihimPG5vP_ZK6u5P87C9fAALhAAPEe4QKi7PG5z3j_7YaBA",
        "CAACAgIAAxkBAAIJS18VnJo3e3Ab31YJIBAY0DbWWXhWAAIkAwACz7vUDm0PI10rjwfcGgQ",
        "CAACAgIAAxkBAAI4uGDbEND8V93aDG2e24YrSHi-QxYYAAJAAAOvxlEaV1XfcKI2zaogBA",
        "CAACAgIAAxkBAAI4t2DbEMwiSD4Ezqvf07G-TpPayVOQAAJ-AgACVp29CkFidF9RbKzkIAQ",
        "CAACAgIAAxkBAAIJj18Wh_ZBEj4O1SXCb3Dbb-hvPPmlAAJiCQACeVziCYqMcuA2PTO4GgQ",
        "CAACAgUAAxkBAAI4wmDbEsV7CXDO577Wt8QB7gXGYAn5AAK8AQAChr5BV4TIzDpMpfkHIAQ",
        "CAACAgUAAxkBAAI4x2DbExt_JZsSEJUFnWB_vy8hfUvDAAKyAgACETFAV-Bxn7axpULxIAQ",
        "CAACAgIAAxkBAAIJkl8WiDPgckyWTmq3PGwwRpmQ16PXAAJXAAMfAUwVF6izXvNT8SwaBA",
        "CAACAgIAAxkBAAI5d2Dcq2rCvnLfvsQutLDeRB8TjRdmAAKMCQAC8UK_BerutaWU09nvIAQ",
        "CAACAgIAAxkBAAI5eGDcq7SlqYfmoJ6YTzX3FjH62zYtAALuAAOWn4wOhTgay3Q1C9sgBA",
        "CAACAgIAAxkBAAI5eWDcq9LahyZjYE7Gj5dWqnCyIBE_AAIOAAPp2BMoE6Y9Q1_4SB8gBA",
        "CAACAgIAAxkBAAI5emDcrBWy-Lv6IU2bxG_5h2qec35OAAIJAgAC8UK_BTg8-O7sx8t_IAQ",
        "CAACAgIAAxkBAAI5e2DcrCGM5LAqRwwUalQAAXVZ8kzy9wACCQgAAhhC7ggQXFBTmuwvBSAE",
        "CAACAgIAAxkBAAI5fGDcrCjdKZQs02eeXF8b5Pa6_HUGAALZAAPEe4QKQmZOr1B0P9ggBA",
        "CAACAgUAAxkBAAI5fWDcrD2o43GBa0MsEhrxi1_vbE0BAALSAQACAcJBV39Vv5Q03ZM-IAQ",
        "CAACAgQAAxkBAAI5fmDcrPmnW7F7KJmlZTrFlt49rRY2AALVAgACT_8wAxKruuw1Yd2tIAQ",
        "CAACAgIAAxkBAAI5f2DcrQ3gA85DMBwWaymiqZovT0QJAAIMAAOOspATvKKj4DttejcgBA",
        "CAACAgIAAxkBAAI5gGDcrRfPBHdy6u0D2gsJoLVgIipRAAItAgACNnYgDjEhsazf7SU_IAQ",
        "CAACAgIAAxkBAAI5gWDcrRusie-mpaQ9SoBD92orVwR-AAIHBQAC8UK_BUaI8SkRK094IAQ",
        "CAACAgIAAxkBAAI5gmDcrSgDTqx50r5nlQ2_NU7qk6_JAAIwAwACz7vUDuGjlyaQSevGIAQ",
        "CAACAgIAAxkBAAI5g2DcrUKvVa-k2fxEGjsoxwOTPDiyAAIuCQACGELuCGbHojCvvLH0IAQ",
        "CAACAgIAAxkBAAI5hGDcrU7ay0k7EvJbKuRjuRZpsqwNAAJfAANSiZEjTXi8DMB8-SogBA",
        "CAACAgUAAxkBAAI5hWDcrXWtn9jNU_mZQBkdAkPLD4SSAAKmAwAC6QrIA3mzkAhrhbH0IAQ",
        "CAACAgIAAxkBAAI5hmDcrYXgilduwv7Mt_st4ecB4gVfAAIPAwACusCVBVPH5VmM0hyyIAQ",
        "CAACAgIAAxkBAAI5h2DcrY5mO5fr7oh4oGOhsMFjfSZfAAJmAAPANk8TUQouVCqCfAUgBA",
        "CAACAgIAAxkBAAI5iGDcrZ7uyOVzjKYhPcCeuYM0xZfyAAJQAAMfAUwVcBAjn_egzq0gBA",
        "CAACAgIAAxkBAAI5i2Dcrmr_PcGLmBqmAAGKdO_TjB0FLAACMwADWbv8JRUD_CxZVMH7IAQ",
        "CAACAgIAAxkBAAI5imDcrl-QVljdhxxNqjEBC_hKSJm4AAIfAAOvxlEaXOcVuSobGHQgBA",
        "CAACAgIAAxkBAAI5iWDcrlNY-Vh-X1bJErvf1sSqU_k7AAJAAQACUomRIza9WNpLrJDoIAQ",
        "CAACAgIAAxkBAAI5lGDcr2JmDHpcVsBEcnWxfGqkLaAdAAJFAAMNttIZjBr_PIJ9KtggBA",
        "CAACAgIAAxkBAAI7x2Ek6MBvF0sJQlarpxHEtWntxHZdAAINDgACbTF5SQS2aZWlIIItIAQ",
        "CAACAgQAAxkBAAI7yGEk6OGDhTXLWOYrI2rUuLv-wLWsAALsAgACT_8wA2BkuAUsrJOmIAQ",
        "CAACAgIAAxkBAAI7y2Ek6YArXGps2uuvuggBKFpZoddCAAIFBQAC8UK_BZcp0EaptAXnIAQ",
        "CAACAgIAAxkBAAI7zGEk6aCtn7-rGsrlMTP8F-pKjYw-AAJ-AAOWn4wOcYMRnixctuUgBA",
        "CAACAgIAAxkBAAI7zWEk6huTHhGMwCg9LldSZXTKAgtxAALVCQAC8UK_Bfv43jOBssSzIAQ",
        "CAACAgIAAxkBAAI7zmEk6pAC06Llv5SpsYtos35x2_uBAAImDAACeEl5SjABOKmPZFTCIAQ",
        "CAACAgIAAxkBAAI7z2Ek6qi1apoWyDCJjrij9XkXfAlkAAIsAQAC9wLID6abwCn6K4ldIAQ",
    )

    private val badStickers = setOf(
        "CAACAgIAAxkBAAIJYl8WgCadydPEHsn8m3Zj11fUJe5cAAJTBAACa8TKCj3zLQSMAAHUshoE",
        "CAACAgIAAxkBAAIJY18WgC9yX5XGzCEYPCJHXI9ELZqqAAIdAQACYyviCcmu_Eb_OpfCGgQ",
        "CAACAgIAAxkBAAIJZF8WgDua5wQQ5HO1iKCvNrXG0B5dAAJYAQACYyviCUGY0kTDklu3GgQ",
        "CAACAgIAAxkBAAIJZV8WgD9zNMm42QH4_baLrEqIPZylAAJgCQACeVziCSWHSkOIrb0eGgQ",
        "CAACAgIAAxkBAAIJZl8WgETaBqdrWRh3toYcL9h11lYpAALjCAACCLcZAqhiD9a4x0BrGgQ",
        "CAACAgUAAxkBAAIJZ18WgEdDX9OK5xUJz1STWuZUVX96AAJ9AwAC6QrIA60D0stxYv2mGgQ",
        "CAACAgIAAxkBAAIJaF8WgFASM7bpj-j7_Uz-jyiHMI_lAAIMAQACVp29Cqpv9dJA3OI9GgQ",
        "CAACAgIAAxkBAAIJaV8WgFzJl9AQTzXJIYcWXwcQcwMGAAJ9AAP3AsgPLsm7Ct3LIkkaBA",
        "CAACAgIAAxkBAAIJal8WgGLg1NhN5ks7mVUTiEhOCmoXAAJ3AAPkoM4HJeKgUTKBHKsaBA",
        "CAACAgIAAxkBAAIJa18WgG69hmc5a6d24vV6FHwJYJdBAAKhAgACLw_wBuuWE9yacw5aGgQ",
        "CAACAgIAAxkBAAIJbF8WgHmNA1mVcNP1_jkowYdim_KfAALcBQAC-gu2CAVrbzSDr7IeGgQ",
        "CAACAgMAAxkBAAIJbV8WgIOeyWFeeDslyHRKZsadXeSfAAKlBQACv4yQBIanELUTj1vxGgQ",
        "CAACAgIAAxkBAAIJbl8WgJKvQCTheD8cOUSfxDu8EVGSAAL7AAO6wJUF9-eECGzvdbsaBA",
        "CAACAgIAAxkBAAIJb18WgJgHzSe8-yozVHmWFops_Hd1AAJ-AAOc1ucKNwEqwAmfLPEaBA",
        "CAACAgIAAxkBAAIJcF8WgJy6BMGp4CjRtzEtsWADzqj3AALbAwACRvusBFAotbLO5Xr6GgQ",
        "CAACAgIAAxkBAAIJcV8WgKMTjJ7QAAEwWx4INPOEbwZdQAACdwADpkRIC2VG38pizwfDGgQ",
        "CAACAgIAAxkBAAIJcl8WgKwmCFw_5Jk44QguB0pZMQGXAAJ2AAN8l30Le9tVTmzP0-8aBA",
        "CAACAgIAAxkBAAIJc18WgLG0g1lL4c0dbqJ-MVl2xA9OAAIIAgAC3PKrB9wfMThSyktwGgQ",
        "CAACAgIAAxkBAAIJdF8WgLyvm7scUeGkKo8CJ1Xs9a4KAAL9AAM2diAO4PMb1BfZQpMaBA",
        "CAACAgIAAxkBAAIJdl8WglCb-0drPxdhlNmHCXdR_AE_AAIDAwAChmBXDtcEnzRXnuejGgQ",
        "CAACAgIAAxkBAAIJd18WglWIu2WmKC7jklabf-d11GduAAIlAwACnNbnCgAB0udXwyXKPxoE",
        "CAACAgIAAxkBAAIJeF8Wgls46s9Z1dTUBv8suIMNAprDAALlAAPEe4QKCJwD6jAVaaUaBA",
        "CAACAgIAAxkBAAIJeV8WgmHTWK_n_yZPClEgDqUKomWpAAL1BwACYyviCa-zO_3ScB8TGgQ",
        "CAACAgIAAxkBAAIJel8WgmeG9nQhdpkxoTQitp-9eKLfAAKMAAMfAUwVzVhIJLQKmaIaBA",
        "CAACAgIAAxkBAAIJe18Wgm7D7YNSkwrbSi8OcjWXmCcQAAKGAQACihKqDtQEq1Y5m_yfGgQ",
        "CAACAgUAAxkBAAIJfF8WgnguQm0k46yhyBSRwXZnYUJmAAJ4AwAC6QrIA9OAY5YOT1JnGgQ",
        "CAACAgUAAxkBAAIJfV8Wgny-k8Iy2t3N5H8o8H24OhhqAALDAwAC6QrIA8UWW3R2DVzfGgQ",
        "CAACAgUAAxkBAAIJfl8WgoJeGFhd8JxyES0cF73YwkjeAAKPAwAC6QrIAzdw6Tx8FEZIGgQ",
        "CAACAgIAAxkBAAIJf18Wgo1OdW50dJtbc08k6ZCExEIhAALoAQACygMGC7r72N5wzGG7GgQ",
        "CAACAgIAAxkBAAIJgF8Wgpgsfp31oJQoshX09Q6_K2RjAAKTAAP3AsgPJeWS_-k7iFUaBA",
        "CAACAgIAAxkBAAIJgV8WgqRij08Nn3jUc-0wLSorHsLyAAL5AANWnb0KlWVuqyorGzYaBA",
        "CAACAgIAAxkBAAIJgl8Wgq68brT5GC4zt-WPHcYYTDd6AAJHAANSiZEj2Audy00CRw0aBA",
        "CAACAgIAAxkBAAIJg18WgrVhBCf-mVf9mx3ugRuaw7QqAAJlAAOWn4wOHfGZDqS0IikaBA",
        "CAACAgIAAxkBAAIJhF8WgsCCF9Xv3oZXsuJhyGaCz9GIAAIgAAPkoM4HJUDHoipmCFQaBA",
        "CAACAgIAAxkBAAIJhV8WgseGFo_xmIsB2uBQ3qAUgLWNAAI6AwACxKtoCxkNYQ8HmB4vGgQ",
        "CAACAgIAAxkBAAIJhl8WgtXFYOYkRWmxjjZLy2uoScg3AALkAgACRxVoCeOpfo6tHHz-GgQ",
        "CAACAgIAAxkBAAIJh18Wgt_mA7aRrzJzPAh995hVLlkLAAJDAAO2j0oJI4YMUY38hXsaBA",
        "CAACAgIAAxkBAAIJiF8WguroLYVozNpYo9jyAw6PjCCJAALTAwACxKtoC5_3eyr8VMfsGgQ",
        "CAACAgIAAxkBAAIJiV8WgvoQ_4OBLhzuTW87dLph0Uv3AAIqAAOhjEELUTaM8bmmvIMaBA",
        "CAACAgIAAxkBAAIJil8Wgwh1N__fBLpG-4Dj3W5cm3sgAAIiBQACa8TKCnMzzBLXvW-LGgQ",
        "CAACAgIAAxkBAAI5jGDcrswYVc95fBhfZgI6pLi0uD6_AAJUAAOvxlEallJjH6gDA0YgBA",
        "CAACAgUAAxkBAAI5jWDcrt16QLqAvELyaaPolMk1nbRjAAJ3AQACboJAV3saMxV1A4-5IAQ",
        "CAACAgIAAxkBAAI5jmDcrucutGzl4uU7H4c7r3kbUiAgAAIQCQACGELuCIaEomA6RbR4IAQ",
        "CAACAgIAAxkBAAI5j2DcrvM7ap-i8qr75r24DhxzhWsOAAL7AAO6wJUF9-eECGzvdbsgBA",
        "CAACAgUAAxkBAAI5kGDcrvoRQeuTyhO0IXCUPNxzxviGAAJDAwACyo84VyMQ6rsEsj4oIAQ",
        "CAACAgIAAxkBAAI5kWDcrxGfVmtwUqqOxnnjkBj2R8crAAIMAQACVp29Cqpv9dJA3OI9IAQ",
        "CAACAgIAAxkBAAI5kmDcryrsb0PtFvp_5lSH6P7Phpf_AALqAAPEe4QKJjjXPLKXJv4gBA",
        "CAACAgIAAxkBAAI5k2Dcr0AEPGJE2OltrZ2cu1cF9EdeAAIiBAAChmBXDp0ToXmfDKbBIAQ",
        "CAACAgIAAxkBAAI5oGDcsPQ2tqXRA7YhID40ApQ73BPTAAIlAAOKl00UYAbm1_VreNYgBA",
        "CAACAgQAAxkBAAI5n2DcsN9ScbXwDXRd67X1wKgbiErrAALKAgACT_8wA3_6orHsWGQIIAQ",
        "CAACAgQAAxkBAAI5nmDcsLr9aQuLmXaOgvrr23TI7ueSAAJfAwACT_8wAzY0FHWeeKmUIAQ",
        "CAACAgQAAxkBAAI5nWDcsLUoa-Khw6FmPUHEumaTVwXiAAJhAwACT_8wA6n6JVTMvW45IAQ",
        "CAACAgIAAxkBAAI5nGDcsJf-a1A_tSqaxpWQ8vV13WmUAAIRAgACNnYgDn4Le9Sc1GaYIAQ",
        "CAACAgIAAxkBAAI5m2DcsEBPIw2Ts21WW70VbSvAFi90AAJkAQACihKqDgUJsHlLd_kHIAQ",
        "CAACAgIAAxkBAAI5mmDcsB7f1muDU7MyF081nH9eEcqOAAKDAAPBnGAMjEXaG6vxAgYgBA",
        "CAACAgUAAxkBAAI5mWDcsA9OvYoVxVfDPK9wv73GXKUDAALDAQACIT9BVxU3nXxwD47IIAQ",
        "CAACAgUAAxkBAAI5mGDcsAlvYQiSy0wGSoBB4aYvEP8uAAIWAgACvmg4V8C7X5YCmtsIIAQ",
        "CAACAgIAAxkBAAI5l2Dcr-6mrJqPwgb3suZ8uAbR5aHwAALJCQAC8UK_Bb4s9k3VGvUYIAQ",
        "CAACAgIAAxkBAAI5lmDcr-a3PhsW8e3AN_TNPkDRA1D0AALlBQAC8UK_BaqB4N4nVb7CIAQ",
        "CAACAgQAAxkBAAI5lWDcr9XrzEzMpNh6DfJd07mtHmS2AAJ4AwACT_8wAy_xB518R7yXIAQ"
    )

    suspend fun getFlagSticker(message: Message, flag: String): SendSticker? {
        val start = System.nanoTime()
        val bot = bot.get() ?: return null
        val sticker = SendSticker()
        var msgText = ""

        when (val result = DbHelper.onFlagPassed(bot.competition, message.chatId, flag)) {
            is DbHelper.FlagCheckResult.CorrectFlag -> {
                sticker.sticker = InputFile(niceStickers.random())
                msgText = "Отлично! +${result.price}"
            }
            is DbHelper.FlagCheckResult.WrongFlag -> {
                sticker.sticker = InputFile(badStickers.random())
                msgText = "Нет такого флага"
            }
            is DbHelper.FlagCheckResult.SolveExists -> {
                sticker.sticker = InputFile(niceStickers.random())
                msgText = "Этот флаг ты уже сдал"
            }
        }

        sticker.chatId = message.chatId.toString()
        val menuButton = InlineKeyboardButton(msgText)
        menuButton.callbackData = DATA_MENU
        sticker.replyMarkup = InlineKeyboardMarkup(listOf(listOf(menuButton)))

        val end = System.nanoTime()
        Logger.debug(tag, "Prepared flag sticker in: ${(end - start) / 1000000} ms")
        return sticker
    }

    suspend fun getStartMessage(message: Message): SendMessage {
        val userName = message.from.userName ?: message.from.firstName
        return getStartMessage(userName, message.chatId)
    }

    suspend fun getStartMessage(callback: CallbackQuery): SendMessage {
        val userName = callback.from.userName ?: callback.from.firstName
        return getStartMessage(userName, callback.message.chatId)
    }

    private suspend fun getStartMessage(userName: String, chatId: Long): SendMessage {
        val start = System.nanoTime()
        val usrName = userName.filter { it in allowedCharacters }
        val playerName = usrName.let { if (it.length > 16) it.substring(0..15) else it }
        val player = DbHelper.getPlayer(chatId) ?: DbHelper.add(PlayerModel(chatId, playerName))
            ?: return getErrorMessage(chatId)

        val msgText = "Привет, я  CTF-бот! Я помогаю проводить соревнования по CTF - раздаю задания и проверяю флаги." +
                "Я буду называть тебя ${player.name}, если ты не против.\n\n" +
                "Чтобы сменить имя, используй команду:\n<i>${Bot.MSG_CHANGE_NAME} __новое_имя__</i>\n" +
                "Только постарайся уложиться в 16 " +
                "символов - больше я не запомню :)\n\n" +
                "Чтобы удалить себя из моей базы данных пришли команду:\n<i>${Bot.MSG_DELETE} __своё_текущее_имя__</i>\n\n" +
                "Подробнее о доступных командах:\n<i>${Bot.MSG_COMMANDS_HELP}</i>"

        val menuButton = InlineKeyboardButton()
        menuButton.text = "Меню"
        menuButton.callbackData = DATA_MENU

        val msg = SendMessage()
        msg.text = msgText
        msg.replyMarkup = InlineKeyboardMarkup(listOf(listOf(menuButton)))
        msg.chatId = chatId.toString()
        msg.enableHtml(true)

        val end = System.nanoTime()
        Logger.debug(tag, "Prepared start message in ${(end - start) / 1000000} ms")
        return msg
    }

    suspend fun getMenuMessage(callback: CallbackQuery): SendMessage {
        val userName = callback.from.userName ?: callback.from.firstName
        return getMenuMessage(userName, callback.message.chatId)
    }

    suspend fun getMenuMessage(message: Message): SendMessage {
        val userName = message.from.userName ?: message.from.firstName
        return getMenuMessage(userName, message.chatId)
    }

    private suspend fun getMenuMessage(userName: String, chatId: Long): SendMessage {
        val start = System.nanoTime()
        val bot = bot.get() ?: return getErrorMessage(chatId)
        val player = DbHelper.getPlayer(chatId) ?: return getStartMessage(userName, chatId)
        val (competitionScore, totalScore) = DbHelper.getCompetitionAndTotalScores(player, bot.competition)

        val msgText = """<b>${bot.competition.name}</b>
                |
                |Привет, <i>${player.name}</i>! Твой текущий счёт: $competitionScore. 
                |Твой общий счёт: $totalScore.
                |Для управления используй кнопки. Чтобы сдать флаг напиши
                |<i>${Bot.MSG_FLAG} __твой_флаг__</i>
                |""".trimMargin()

        val buttonRow1 = listOf(
            InlineKeyboardButton("Текущая таблица лидеров").apply { callbackData = DATA_CURRENT_SCOREBOARD },
            InlineKeyboardButton("Общая таблица лидеров").apply { callbackData = DATA_GLOBAL_SCOREBOARD }
        )
        val buttonRow2 = listOf(
            InlineKeyboardButton("Задания").apply { callbackData = DATA_TASKS }
        )
        val buttonRow3 = listOf(
            InlineKeyboardButton("Доступные команды").apply { callbackData = DATA_COMMANDS }
        )
        val buttonsTable = listOf(buttonRow1, buttonRow2, buttonRow3)

        val msg = SendMessage()
        msg.chatId = chatId.toString()
        msg.enableHtml(true)
        msg.text = msgText
        msg.replyMarkup = InlineKeyboardMarkup(buttonsTable)

        val end = System.nanoTime()
        Logger.debug(tag, "Prepared menu message in ${(end - start) / 1000000} ms")
        return msg
    }

    suspend fun getPasswordRequestMessage(chatId: Long): SendMessage {
        val msgText = "Бот находится в состоянии тестирования. " +
                "Для авторизации пришли мне пароль в формате:\n<i>${Bot.MSG_TESTING_PASSWORD} __пароль__</i>"
        val msg = SendMessage()
        msg.chatId = chatId.toString()
        msg.text = msgText
        msg.enableHtml(true)
        return msg
    }

    suspend fun getPasswordWrongMessage(message: Message): SendMessage {
        val msg = SendMessage()
        msg.chatId = message.chatId.toString()
        msg.text = "Неверный пароль. Доступ запрещён"
        return msg
    }


    suspend fun getTasksMessage(callback: CallbackQuery): SendMessage {
        val start = System.nanoTime()
        val bot = bot.get() ?: return getErrorMessage(callback.message.chatId)
        val player = DbHelper.getPlayer(callback.message.chatId) ?: return getStartMessage(callback)
        val msgText = "<b>${bot.competition.name}</b>\n\nСписок заданий: "
        val buttonsList = arrayListOf<List<InlineKeyboardButton>>()

        val tasksList = DbHelper.getTasksList(player, bot.competition)
        for (task in tasksList) {
            val taskSolved = task.third
            val taskPrice = task.second

            buttonsList.add(listOf(
                InlineKeyboardButton(
                    "${task.first.category} - ${taskPrice}: ${task.first.name} ${if (taskSolved) "\u2705" else ""}"
                ).apply { callbackData = "${Bot.DATA_TASK} ${task.first.id}" }
            ))
        }

        buttonsList.add(listOf(
            InlineKeyboardButton(
                "Menu"
            ).apply { callbackData = DATA_MENU }
        ))

        val msg = SendMessage()
        msg.enableHtml(true)
        msg.chatId = callback.message.chatId.toString()
        msg.text = msgText
        msg.replyMarkup = InlineKeyboardMarkup(buttonsList)

        val end = System.nanoTime()
        Logger.debug(tag, "Prepared task list message in ${(end - start) / 1000000}")
        return msg
    }


    suspend fun getCurrentScoreboard(callback: CallbackQuery): SendMessage {
        val start = System.nanoTime()
        val bot = bot.get() ?: return getErrorMessage(callback.message.chatId)
        val scoreboard = DbHelper.getScoreboard(bot.competition)
        var msgText = """
                <b>${bot.competition.name}</b>
                |
                |Таблица лидеров по текущей игре:
                |
                |<code>
                """.trimMargin()

        scoreboard.forEachIndexed { index, player ->
            val name = player.first.padEnd(16, ' ')
            val number = (index + 1).toString().padStart(3, ' ')
            val score = player.second.toString().padStart(6, ' ')
            msgText += "%s. %s %s\n".format(number, name, score)
        }
        msgText += "</code>"

        val msg = SendMessage()
        msg.chatId = callback.message.chatId.toString()
        msg.enableHtml(true)
        msg.text = msgText
        msg.replyMarkup = InlineKeyboardMarkup(listOf(listOf(
            InlineKeyboardButton("Меню").apply { callbackData = DATA_MENU }
        )))

        val end = System.nanoTime()
        Logger.debug(tag, "Prepared current scoreboard message in ${(end - start) / 1000000} ms")
        return msg
    }

    suspend fun getGlobalScoreboard(callback: CallbackQuery): SendMessage {
        val start = System.nanoTime()
        val bot = bot.get() ?: return getErrorMessage(callback.message.chatId)
        val scoreboard = DbHelper.getScoreboard()
        var msgText = """
                <b>${bot.competition.name}</b>
                |
                |Таблица лидеров по всем играм:
                |
                |<code>
                """.trimMargin()

        scoreboard.forEachIndexed { index, player ->
            val name = player.first.padEnd(16, ' ')
            val number = (index + 1).toString().padStart(3, ' ')
            val score = player.second.toString().padStart(6, ' ')
            msgText += "%s. %s %s\n".format(number, name, score)
        }
        msgText += "</code>"

        val msg = SendMessage()
        msg.chatId = callback.message.chatId.toString()
        msg.enableHtml(true)
        msg.text = msgText
        msg.replyMarkup = InlineKeyboardMarkup(listOf(listOf(
            InlineKeyboardButton("Меню").apply { callbackData = DATA_MENU }
        )))

        val end = System.nanoTime()
        Logger.debug(tag, "Prepared global scoreboard in ${(end - start) / 1000000} ms")
        return msg
    }


    suspend fun getTaskMessage(callback: CallbackQuery, taskId: Long): SendMessage {
        val start = System.nanoTime()
        val bot = bot.get() ?: return getErrorMessage(callback.message.chatId)
        val task = DbHelper.getTask(taskId) ?: return getErrorMessage(callback.message.chatId)
        val attachmentFile = File(task.attachment)
        val taskPrice = DbHelper.getTaskPrice(task)

        val msgText = "<b>${bot.competition.name}</b>\n" +
                "\n${task.name}           ${taskPrice}\n\n${task.description}"
        val msg = SendMessage()
        msg.enableHtml(true)

        msg.chatId = callback.message.chatId.toString()
        msg.text = msgText

        val buttons = arrayListOf<List<InlineKeyboardButton>>()
        if (attachmentFile.exists()) {
            buttons.add(
                listOf(InlineKeyboardButton(attachmentFile.name).apply { callbackData = "$DATA_FILE $taskId" })
            )
        }
        buttons.add(listOf(InlineKeyboardButton("Меню").apply { callbackData = DATA_MENU }))
        msg.replyMarkup = InlineKeyboardMarkup(buttons)

        val end = System.nanoTime()
        Logger.debug(tag, "Prepared task message in ${(end - start) / 1000000} ms")
        return msg
    }

    suspend fun getFileMessage(callback: CallbackQuery, taskId: Long): SendDocument? {
        val start = System.nanoTime()
        val task = DbHelper.getTask(taskId) ?: return null
        val attachmentFile = File(task.attachment)
        if (!attachmentFile.exists()) return null

        val msg = SendDocument()
        msg.chatId = callback.message.chatId.toString()
        msg.document = InputFile(attachmentFile, attachmentFile.name)
        msg.replyMarkup = InlineKeyboardMarkup(listOf(listOf(
            InlineKeyboardButton("Меню").apply { callbackData =  DATA_MENU}
        )))

        val end = System.nanoTime()
        Logger.debug(tag, "Prepared task file message in ${(end - start) / 1000000} ms")
        return msg
    }

    suspend fun getErrorMessage(chatId: Long): SendMessage {
        val msgText = "Ой, возникла какая-то ошибка. Свяжитесь с @awawa0_0 для обратной связи."
        val msg = SendMessage()
        msg.chatId = chatId.toString()
        msg.replyMarkup = InlineKeyboardMarkup(
            listOf(listOf(InlineKeyboardButton("Меню").apply { callbackData = DATA_MENU }))
        )
        msg.text = msgText
        return msg
    }

    suspend fun getUnknownMessage(message: Message): SendMessage {
        val msgText = "Это что? Эльфийский? Я не понимаю. Используй кнопки, пожалуйста."
        val buttonsTable = listOf(listOf(InlineKeyboardButton("Меню").apply { callbackData = DATA_MENU }))

        val msg = SendMessage()
        msg.chatId = message.chatId.toString()
        msg.text = msgText
        msg.replyMarkup = InlineKeyboardMarkup(buttonsTable)

        return msg
    }

    suspend fun getConvertMessage(message: Message, content: String): SendMessage {
        val msg = SendMessage()
        var msgText = ""
        val numbers = content.split(" ")

        for (number in numbers) {
            msgText += """
                    Binary: ${Helper.anyToBin(number)}
                    Hex: ${Helper.anyToHex(number)}
                    Decimal: ${Helper.anyToDec(number)}


                """.trimIndent()
        }

        msg.chatId = message.chatId.toString()
        msg.text = msgText
        msg.replyMarkup = InlineKeyboardMarkup(
            listOf(
                listOf(
                    InlineKeyboardButton("Меню").apply { callbackData = DATA_MENU }
                )
            )
        )

        return msg
    }

    suspend fun getToHexMessage(message: Message, content: String): SendMessage {

        val msg = SendMessage()
        var msgText = ""
        val numbers = content.split(" ")

        for (number in numbers) {
            msgText += Helper.anyToHex(number)
        }

        msg.chatId = message.chatId.toString()
        msg.text = msgText
        msg.replyMarkup = InlineKeyboardMarkup(
            listOf(
                listOf(
                    InlineKeyboardButton("Меню").apply { callbackData = DATA_MENU }
                )
            )
        )

        return msg
    }

    suspend fun getToDecMessage(message: Message, content: String): SendMessage {
        val msg = SendMessage()
        var msgText = ""
        val numbers = content.split(" ")

        for (number in numbers) {
            msgText += Helper.anyToDec(number)
        }

        msg.chatId = message.chatId.toString()
        msg.text = msgText
        msg.replyMarkup = InlineKeyboardMarkup(
            listOf(
                listOf(
                    InlineKeyboardButton("Меню").apply { callbackData = DATA_MENU }
                )
            )
        )

        return msg
    }

    suspend fun getToBinMessage(message: Message, content: String): SendMessage {
        val msg = SendMessage()
        var msgText = ""
        val numbers = content.split(" ")

        for (number in numbers) {
            msgText += Helper.anyToBin(number)
        }

        msg.chatId = message.chatId.toString()
        msg.text = msgText
        msg.replyMarkup = InlineKeyboardMarkup(
            listOf(
                listOf(
                    InlineKeyboardButton("Меню").apply { callbackData = DATA_MENU }
                )
            )
        )

        return msg
    }

    suspend fun getToStringMessage(message: Message, content: String): SendMessage {
        val msg = SendMessage()
        var msgText = ""
        val numbers = content.split(" ")

        for (number in numbers) {
            val char = Helper.anyToDec(number).trim().toLong()
            if (char != -1L) {
                msgText += NumbersUtils.numToChar(char)
            } else {
                msgText += "."
            }
        }

        if (msgText.trim().isEmpty()) {
            msgText = msgText.trim() + "."
        }

        msg.chatId = message.chatId.toString()
        msg.text = msgText
        msg.replyMarkup = InlineKeyboardMarkup(
            listOf(
                listOf(
                    InlineKeyboardButton("Меню").apply { callbackData = DATA_MENU }
                )
            )
        )

        return msg
    }

    suspend fun getMessageToPlayer(id: Long, text: String): SendMessage {
        val msg = SendMessage()
        msg.chatId = id.toString()
        msg.text = text
        msg.replyMarkup = InlineKeyboardMarkup(
            listOf(
                listOf(
                    InlineKeyboardButton("Меню").apply { callbackData = DATA_MENU }
                )
            )
        )

        return msg
    }

    suspend fun getRotMessage(message: Message, content: String): SendMessage {
        val msg = SendMessage()
        msg.chatId = message.chatId.toString()
        msg.replyMarkup = InlineKeyboardMarkup(
            listOf(
                listOf(
                    InlineKeyboardButton("Меню").apply { callbackData = DATA_MENU }
                )
            )
        )

        val splat = content.trim().split(" ")

        try {
            val key = splat[0].toInt()
            val text = splat.slice(1 until splat.size).joinToString(" ")
            msg.text = Rot.rotate(text, key)
        } catch (e: Exception) {
            msg.text = "-1"
        }

        return msg
    }

    suspend fun getRotBruteMessage(message: Message, content: String): SendMessage {
        val msg = SendMessage()
        msg.chatId = message.chatId.toString()

        val msgText = StringBuilder()
        for (key in 0 until Rot.ALPHABET_LENGTH) {
            msgText.append("Key: $key  Text: ${Rot.rotate(content, key)}\n")
        }
        msg.text = msgText.toString()
        msg.replyMarkup = InlineKeyboardMarkup(
            listOf(
                listOf(
                    InlineKeyboardButton("Меню").apply { callbackData = DATA_MENU }
                )
            )
        )

        return msg
    }

    suspend fun getCheckMagicMessage(message: Message, content: String): SendMessage {
        val msg = SendMessage()
        msg.chatId = message.chatId.toString()

        val msgText = StringBuilder()
        val replyMarkup = ArrayList<List<InlineKeyboardButton>>()

        val magicCheck = MagicNumbers.findMagic(content.trim())

        msgText.append("Результаты поиска")
        for ((i, match) in magicCheck.withIndex()) {
            replyMarkup.add(
                listOf(
                    InlineKeyboardButton(
                        "${i + 1}. ${match.first.formatName} - ${if (match.second) "Полное совпадение" else "Неполное совпадение"}"
                    ).apply {
                        match.first.callback
                    }
                )
            )
        }

        msg.text = msgText.toString()

        replyMarkup.add(listOf(InlineKeyboardButton("Меню").apply{ callbackData = DATA_MENU }))
        msg.replyMarkup = InlineKeyboardMarkup(replyMarkup)

        return msg
    }

    suspend fun getChangeNameMessage(message: Message, newName: String): SendMessage {
        val msg = SendMessage()
        msg.chatId = message.chatId.toString()
        if (newName.isBlank() || newName.length > 16 || newName.any { it !in allowedCharacters}) {
            val msgText = "Имя пользователя не соответствует требованиям формата и не будет изменено."
            msg.text = msgText
            msg.replyMarkup = InlineKeyboardMarkup(listOf(
                listOf(InlineKeyboardButton("Меню").apply { callbackData = DATA_MENU }))
            )
            return msg
        }

        val player = DbHelper.getPlayer(message.chatId) ?: return getStartMessage(message)
        player.name = newName
        player.updateEntity()
        val msgText = "Имя пользователя успешно изменено. Поздравляю, ты теперь официально: <b>${player.name}</b>!"
        msg.text = msgText
        msg.enableHtml(true)
        msg.replyMarkup = InlineKeyboardMarkup(listOf(
            listOf(InlineKeyboardButton("Меню").apply { callbackData = DATA_MENU }))
        )
        return msg
    }

    suspend fun getDeleteMessage(message: Message, content: String): SendMessage {
        val msg = SendMessage()
        msg.chatId = message.chatId.toString()
        val player = DbHelper.getPlayer(message.chatId) ?: return getStartMessage(message)
        if (content != player.name) {
            val msgText = "Нет, так не пойдёт. Чтобы удалить пользователя пришли своё имя."
            msg.text = msgText
            msg.replyMarkup = InlineKeyboardMarkup(listOf(
                listOf(InlineKeyboardButton("Меню").apply { callbackData = DATA_MENU }))
            )
            return msg
        }

        if (!DbHelper.delete(player)) return getErrorMessage(message.chatId)

        msg.text = "Пользователь удалён. Теперь я тебя не знаю."
        msg.replyMarkup = InlineKeyboardMarkup(listOf(
            listOf(InlineKeyboardButton("Меню").apply { callbackData = DATA_MENU }))
        )
        return msg
    }

    suspend fun getMagicData(callback: CallbackQuery, content: String): SendMessage {
        val msg = SendMessage()
        msg.chatId = callback.message.chatId.toString()
        msg.enableHtml(true)
        msg.text = MagicNumbers.getDataForMagic(content.trim())
        msg.replyMarkup = InlineKeyboardMarkup(listOf(
            listOf(InlineKeyboardButton("Меню").apply { callbackData = DATA_MENU }))
        )
        return msg
    }

    suspend fun getCommandsHelpMessage(message: Message): SendMessage {
        return getCommandsHelpMessage(message.chatId)
    }
    suspend fun getCommandsHelpMessage(callback: CallbackQuery): SendMessage {
        return getCommandsHelpMessage(callback.message.chatId)
    }

    private suspend fun getCommandsHelpMessage(chatId: Long): SendMessage {
        val msg = SendMessage()
        msg.chatId = chatId.toString()
        msg.text = """
                Список команд, поддерживаемых ботом. Заметьте, что бот распознаёт десятичные, двоичные и шестнадцатеричные числа. Двоичные числа должны иметь префикс '0b', а шестнадцатеричные '0x'.
                В массивах числа должны быть разделены пробелом. Числа ограничены диапазоном [0:9223372036854775807]

                ${Bot.MSG_FLAG} <string> - проверяет флаг. Если переданная строка является флагом к какому-либо заданию, это задание будет зачтено как решенное.
                
                ${Bot.MSG_COMMANDS_HELP} - показать это сообщение.
                
                ${Bot.MSG_DELETE} __твоё_имя_пользователя__ - удаляет текущего пользователя из базы данных. Имя пользователя требуется для подтверждения удаления.
                  
                ${Bot.MSG_CHANGE_NAME} __новое_имя_пользователя__ - меняет имя текущего пользователя на новое. Имя пользователя не является его уникальным идентификатором. Разные пользователи могут иметь одинаковые имена. Имя пользователя ограничено 16 символами из алфавита: $allowedCharacters 

                ${Bot.MSG_CONVERT} <array of numbers> - переводит массив чисел в двоичную, десятичную и шестнадцатеричную системы счисления.

                ${Bot.MSG_TO_HEX} <array of numbers> - переводит массив чисел в шестнадцатеричную систему счисления.

                ${Bot.MSG_TO_DEC} <array of numbers> - переводит массив чисел в десятичную систему счисления.

                ${Bot.MSG_TO_BIN} <array of numbers> - переводит массив чисел в двоичную систему счисления.

                ${Bot.MSG_TO_STRING} <array of numbers> - переводит массив чисел в одну строку. Числа ограничены 16 битами. Если передано число длиннее 16 бит, будут использованы младшие его 16 бит.

                ${Bot.MSG_ROT} <key> <text> - преобразует текст по алгоритму ROT13 (Шифрование Цезаря) с заданным ключом. Ключ может быть положительным или отрицательным.

                ${Bot.MSG_ROT_BRUTE} <text> - расшифровывает текст по алгоритму ROT13 (Шифрование Цезаря) со всеми возможными вариантами ключа.

                ${Bot.MSG_CHECK_MAGIC} <magic_number> - помогает определить тип файла по магическому числу. Магические числа должны быть указаны в шестнадцатеричном формате без префикса '0x', пример: ff d8. Магическими числами считаются не только сигнатуры файлов (первые n байт), но и другие, характерные для файлов последовательности. Например, "49 44 41 54" - сектор данных (IDAT) PNG файла.
            """.trimIndent()
        msg.replyMarkup = InlineKeyboardMarkup(
            listOf(
                listOf(
                    InlineKeyboardButton("Меню").apply { callbackData = DATA_MENU }
                )
            )
        )

        return msg
    }
}