import { createControl } from './controls'

export const Button = createControl((self) => ({
  click: () => self.click(),
  value: () => self.innerText(),
  isVisible: () => self.isVisible(),
  isDisabled: () => self.isDisabled()
}))
