/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {dates, Device, Event, HtmlComponent, scrollbars, TimePickerModel, TimePickerTouchPopup, Widget} from '../index';
import $ from 'jquery';
import {Optional} from '../types';
import {ScrollbarInstallOptions} from '../scrollbar/scrollbars';
import TimePickerEventMap from './TimePickerEventMap';
import {EventMapOf, EventModel} from '../events/EventEmitter';

export default class TimePicker extends Widget {
  declare model: TimePickerModel;
  declare eventMap: TimePickerEventMap;

  preselectedTime: Date;
  selectedTime: Date;
  viewDate: Date;
  resolution: number;

  constructor() {
    super();

    this.preselectedTime = null;
    this.selectedTime = null;
    this.resolution = null;
  }

  protected override _init(options: TimePickerModel) {
    super._init(options);
    this.resolution = options.timeResolution;
  }

  protected override _render() {
    this.$container = this.$parent
      .appendDiv('time-picker')
      .toggleClass('touch-only', Device.get().supportsOnlyTouch());
    this.$parent.appendDiv('time-picker-separator');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);

    this._renderTimeSelection();
    this._installScrollbars();
  }

  protected _renderTimeSelection() {
    let i,
      date = dates.trunc(new Date()),
      now = dates.ceil(new Date(), this.resolution),
      currentHours = 0,
      $hourRow,
      $time;
    let $box = this.$parent.makeDiv('day-table');
    for (i = 0; i < 24; i++) {
      // reset minutes always every hour line starts with 00
      date.setMinutes(0);
      currentHours = date.getHours();

      $hourRow = $box.appendDiv('hour-row');
      let $hour = $hourRow.appendDiv('cell  hours')
        .data('time', new Date(date))
        .on('click', this._onTimeClick.bind(this));
      if (now.getHours() === date.getHours()) {
        $hour.addClass('now');
      }
      $hour.appendSpan('text')
        .text(dates.format(date, this.session.locale, 'HH'));

      while (currentHours === date.getHours()) {
        $time = $hourRow.appendDiv('cell minutes')
          .data('time', new Date(date))
          .on('click', this._onTimeClick.bind(this));

        $time.appendSpan('text').text(dates.format(date, this.session.locale, 'mm'));
        if (dates.isSameTime(now, date)) {
          $time.addClass('now');
        }
        date.setMinutes(date.getMinutes() + this.resolution);
      }
    }
    $box.appendTo(this.$container);
    return $box;
  }

  protected override _installScrollbars(options?: Optional<ScrollbarInstallOptions, 'parent'>) {
    this._uninstallScrollbars();

    super._installScrollbars({
      axis: 'y'
    });
  }

  protected _scrollTo($scrollTo: JQuery) {
    if (!$scrollTo) {
      return;
    }
    if (this.parent instanceof TimePickerTouchPopup) {
      // setTimeout seems to be necessary on ios
      setTimeout(() => {
        if (this.rendered) {
          scrollbars.scrollTo(this.$container, $scrollTo, 'center');
        }
      });
    } else {
      scrollbars.scrollTo(this.$container, $scrollTo, 'center');
    }
  }

  preselectTime(time: Date) {
    if (time) {
      // Clear selection when a date is preselected
      this.setSelectedTime(null);
    }
    this.setPreselectedTime(time);
  }

  /**
   * @internal, use {@link preselectDate} to preselect a date
   */
  setPreselectedTime(preselectedTime: Date) {
    this.setProperty('preselectedTime', preselectedTime);
  }

  protected _renderPreselectedTime() {
    let $scrollTo: JQuery;
    this.$container.find('.cell').each((i, elem) => {
      let $time = $(elem),
        time = $time.data('time');
      $time.removeClass('preselected');
      if (this.preselectedTime) {
        if ($time.hasClass('hours') && this.preselectedTime.getHours() === time.getHours()) {
          $time.addClass('preselected');
          $scrollTo = $time;
        } else if ($time.hasClass('minutes') && dates.isSameTime(this.preselectedTime, time)) {
          $time.addClass('preselected');
          $scrollTo = $time;
        }
      }
    });
    this._scrollTo($scrollTo);
  }

  selectTime(time: Date) {
    if (time) {
      // Clear selection when a date is preselected
      this.setPreselectedTime(null);
    }
    this.setSelectedTime(time);
  }

  /**
   * @internal, use selectDate to select a date
   */
  setSelectedTime(selectedTime: Date) {
    this.setProperty('selectedTime', selectedTime);
  }

  protected _renderSelectedTime() {
    let $scrollTo: JQuery;
    this.$container.find('.cell').each((i, elem) => {
      let $time = $(elem),
        time = $time.data('time');
      $time.removeClass('selected');
      if (this.selectedTime) {
        if ($time.hasClass('hours') && this.selectedTime.getHours() === time.getHours()) {
          $time.addClass('selected');
          $scrollTo = $time;
        } else if ($time.hasClass('minutes') && dates.isSameTime(this.selectedTime, time)) {
          $time.addClass('selected');
          $scrollTo = $time;
        }
      }
    });
    this._scrollTo($scrollTo);
  }

  shiftSelectedTime(hourUnits: number, minuteUnits: number, secondUnits: number) {
    let time = this.preselectedTime;
    if (this.selectedTime) {
      time = dates.shiftTime(this.selectedTime, hourUnits, minuteUnits * this.resolution, secondUnits);
    }
    if (!time) {
      return; // do nothing when no date was found
    }
    this.selectTime(this._snapToTimeGrid(time));
  }

  protected _snapToTimeGrid(time: Date): Date {
    if (!time) {
      return time;
    }
    let min = time.getMinutes();
    min = (parseInt((min / this.resolution) + '') * this.resolution);
    time.setMinutes(min);
    return time;
  }

  protected _onTimeClick(event: JQuery.ClickEvent<HTMLDivElement>) {
    let $target = $(event.currentTarget);
    let time = new Date($target.data('time'));
    this.selectTime(time);
    this.trigger('timeSelect', {
      time: time
    });
  }

  override trigger<K extends string & keyof EventMapOf<TimePicker>>(type: K, eventOrModel?: Event | EventModel<EventMapOf<TimePicker>[K]>): EventMapOf<TimePicker>[K] {
    return super.trigger(type, eventOrModel);
  }
}